
import json
import re
import sys
from pathlib import Path


ROOT = Path(__file__).parent
MOCK_RESULTS = ROOT / "mock-results"
REAL_RESULTS = ROOT / "real-results"


def adapt(case_id, source_path=None):
    if source_path:
        markdown_path = Path(source_path)
        output_path = REAL_RESULTS / f"{case_id}-adapted.json"
    else:
        markdown_path = MOCK_RESULTS / f"{case_id}-claude-review.md"
        output_path = MOCK_RESULTS / f"{case_id}-adapted.json"

    if not markdown_path.exists():
        raise ValueError(f"Claude review file not found: {markdown_path}")

    text = markdown_path.read_text(encoding="utf-8")

    # Parse each severity heading and the text up to the next finding
    # or major section. This retains multiple findings from one review.
    pattern = re.compile(
        r"^### \[(CRITICAL|HIGH|MEDIUM|LOW|INFO)\]\s+(.+?)\s*$"
        r"(.*?)(?=^### \[|^## |\Z)",
        re.MULTILINE | re.DOTALL,
    )

    findings = []
    for match in pattern.finditer(text):
        severity, title, body = match.groups()
        combined = f"{title}\n{body}"

        # Explicit mapping: map only recognised PII-logging language.
        if re.search(
            r"email written to stdout|PII written to stdout|"
            r"customer email.*(?:log|stdout)|"
            r"PII.*(?:log|stdout)|System\.out\.println",
            combined,
            re.IGNORECASE,
        ):
            rule = "CIAM-PII-LOGGING"
        elif re.search(
            r"controller depends on the repository|"
            r"repository directly|layering",
            combined,
            re.IGNORECASE,
        ):
            rule = "CIAM-CONTROLLER-REPOSITORY-DEPENDENCY"
        elif re.search(
            r"endpoint exposes the domain object|"
            r"exposes the domain object|BOLA|excessive data exposure",
            combined,
            re.IGNORECASE,
        ):
            rule = "CIAM-API-DOMAIN-EXPOSURE"
        else:
            rule = "UNMAPPED"

        findings.append({
            "finding": True,
            "severity": severity,
            "title": title.strip(),
            "rule": rule,
            "evidence": extract_evidence(body),
            "recommendation": extract_recommendation(body),
        })

    output = {
        "source": str(markdown_path),
        "findings": findings,
    }

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps(output, indent=2) + "\n",
        encoding="utf-8",
    )

    print(f"Review input: {markdown_path}")
    print(f"Findings extracted: {len(findings)}")
    print(f"Normalised result: {output_path}")

    for item in findings:
        print(f"  [{item['severity']}] {item['title']} -> {item['rule']}")


def extract_evidence(body):
    match = re.search(
        r"\*\*Evidence:\*\*\s*(.*?)(?=\n\s*(?:-\s*)?\*\*Expected:|\Z)",
        body,
        re.DOTALL,
    )
    return match.group(1).strip() if match else ""


def extract_recommendation(body):
    match = re.search(
        r"\*\*Recommendation:\*\*\s*(.*?)(?=\n### |\n## |\Z)",
        body,
        re.DOTALL,
    )
    return match.group(1).strip() if match else ""


def main():
    if len(sys.argv) not in (2, 3):
        raise SystemExit(
            "Usage: python evaluation\\adapt_claude_review.py "
            "<CASE-ID> [REVIEW-MARKDOWN-PATH]"
        )

    source_path = sys.argv[2] if len(sys.argv) == 3 else None
    adapt(sys.argv[1], source_path)


if __name__ == "__main__":
    main()
