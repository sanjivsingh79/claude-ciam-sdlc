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


def evaluate(case_id):
    golden = find_golden_case(case_id)
    result_path = RESULTS / f"{case_id}-adapted.json"

    if not result_path.exists():
        raise ValueError(f"AI result not found: {result_path}")

    result = load_json(result_path)
    expected = golden["expected"]

    findings = result.get("findings", [])

    if not findings:
        raise ValueError(
            f"No findings returned by adapter for case {case_id}"
        )

    actual = findings[0]

    checks = {
        "finding": actual.get("finding") == expected["finding"],
        "severity": actual.get("severity") == expected["severity"],
        "rule": actual.get("rule") == expected["rule"],
    }

    print(f"\nEvaluation: {case_id}")

    for name, passed in checks.items():
        print(f"  {name}: {'PASS' if passed else 'FAIL'}")

    if not all(checks.values()):
        raise SystemExit(1)

    print("Overall: PASS")


def main():
    if len(sys.argv) != 2:
        raise SystemExit(
            "Usage: python evaluation/evaluate_results.py <CASE-ID>"
        )

    evaluate(sys.argv[1])


if __name__ == "__main__":
    main()