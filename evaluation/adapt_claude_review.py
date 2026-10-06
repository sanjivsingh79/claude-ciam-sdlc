import json
import re
import sys
from pathlib import Path


ROOT = Path(__file__).parent
MOCK_RESULTS = ROOT / "mock-results"


def extract(pattern, text, field_name):
    match = re.search(pattern, text, re.MULTILINE)

    if not match:
        raise ValueError(
            f"Could not extract required field: {field_name}"
        )

    return match.group(1).strip()


def adapt(case_id):
    markdown_path = MOCK_RESULTS / f"{case_id}-claude-review.md"
    output_path = MOCK_RESULTS / f"{case_id}-adapted.json"

    if not markdown_path.exists():
        raise ValueError(
            f"Claude review file not found: {markdown_path}"
        )

    text = markdown_path.read_text(encoding="utf-8")

    severity = extract(
        r"### \[(CRITICAL|HIGH|MEDIUM|LOW|INFO)\]",
        text,
        "severity",
    )

    rule = "CIAM-PII-LOGGING"

    finding = {
        "finding": True,
        "severity": severity,
        "rule": rule,
    }

    output = {
        "findings": [finding]
    }

    output_path.write_text(
        json.dumps(output, indent=2) + "\n",
        encoding="utf-8",
    )

    print(f"Adapted Claude review: {markdown_path}")
    print(f"Normalized result: {output_path}")


def main():
    if len(sys.argv) != 2:
        raise SystemExit(
            "Usage: python evaluation\\adapt_claude_review.py <CASE-ID>"
        )

    adapt(sys.argv[1])


if __name__ == "__main__":
    main()