
import json
import sys
from pathlib import Path


ROOT = Path(__file__).parent
GOLDEN = ROOT / "golden-dataset"
RESULTS = ROOT / "mock-results"


def load_json(path):
    with path.open(encoding="utf-8") as file:
        return json.load(file)


def find_golden_case(case_id):
    for path in GOLDEN.rglob("*.json"):
        case = load_json(path)
        if case["id"] == case_id:
            return case

    raise ValueError(f"Golden case not found: {case_id}")


def evaluate(case_id, result_file=None):
    golden = find_golden_case(case_id)

    if result_file:
        result_path = Path(result_file)
    else:
        result_path = RESULTS / f"{case_id}-adapted.json"

    if not result_path.exists():
        raise ValueError(f"AI result not found: {result_path}")

    result = load_json(result_path)
    expected = golden["expected"]
    findings = result.get("findings", [])
    expected_finding = expected["finding"]

    print(f"\nEvaluation: {case_id}")
    print(f"Result source: {result_path}")

    if not expected_finding:
        # Negative case: no confirmed finding should be reported.
        confirmed_findings = [
            item for item in findings
            if item.get("finding") is True
        ]

        passed = len(confirmed_findings) == 0
        print(
            "  false_positive_check: "
            f"{'PASS' if passed else 'FAIL'}"
        )

        if confirmed_findings:
            print("  Unexpected findings:")
            for item in confirmed_findings:
                print(
                    f"    [{item.get('severity')}] "
                    f"{item.get('title', '(untitled)')} "
                    f"-> {item.get('rule')}"
                )

        print(f"Overall: {'PASS' if passed else 'FAIL'}")
        if not passed:
            raise SystemExit(1)
        return

    # Positive case: locate the finding matching the expected rule.
    actual = next(
        (
            item for item in findings
            if item.get("rule") == expected["rule"]
            and item.get("finding") is True
        ),
        None,
    )

    if actual is None:
        print("  finding: FAIL - expected rule not found")
        print(f"  severity: FAIL - expected {expected['severity']}")
        print(f"  rule: FAIL - expected {expected['rule']}")
        print("  evidence_present: FAIL")
        print("  recommendation_present: FAIL")
        print("Overall: FAIL")
        raise SystemExit(1)

    checks = {
        "finding": actual.get("finding") == expected["finding"],
        "severity": actual.get("severity") == expected["severity"],
        "rule": actual.get("rule") == expected["rule"],
        "evidence_present": bool(actual.get("evidence")),
        "recommendation_present": bool(actual.get("recommendation")),
    }

    for name, passed in checks.items():
        print(f"  {name}: {'PASS' if passed else 'FAIL'}")

    if "real-results" in result_path.parts:
        print(
            "\nNote: this is a related real-PR scenario, not an exact "
            f"match for the {case_id} code change."
        )

    if not all(checks.values()):
        print("Overall: FAIL")
        raise SystemExit(1)

    print("Overall: PASS")


def main():
    if len(sys.argv) not in (2, 3):
        raise SystemExit(
            "Usage: python evaluation/evaluate_results.py "
            "<CASE-ID> [RESULT-JSON-PATH]"
        )

    result_file = sys.argv[2] if len(sys.argv) == 3 else None
    evaluate(sys.argv[1], result_file)


if __name__ == "__main__":
    main()
