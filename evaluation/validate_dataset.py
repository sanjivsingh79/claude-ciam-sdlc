import json
from pathlib import Path


ROOT = Path(__file__).parent
GOLDEN = ROOT / "golden-dataset"

REQUIRED_TOP_LEVEL = {
    "id",
    "category",
    "scenario",
    "description",
    "code_change",
    "expected",
}

REQUIRED_EXPECTED = {
    "finding",
    "severity",
    "rule",
    "expected_behavior",
}


def load_cases():
    return sorted(GOLDEN.rglob("*.json"))


def validate_case(path):
    with path.open(encoding="utf-8") as file:
        case = json.load(file)

    missing = REQUIRED_TOP_LEVEL - case.keys()
    if missing:
        raise ValueError(
            f"{path}: missing top-level fields: {sorted(missing)}"
        )

    missing_expected = REQUIRED_EXPECTED - case["expected"].keys()
    if missing_expected:
        raise ValueError(
            f"{path}: missing expected fields: {sorted(missing_expected)}"
        )

    if not case["id"].strip():
        raise ValueError(f"{path}: id must not be empty")

    if not isinstance(case["expected"]["finding"], bool):
        raise ValueError(
            f"{path}: expected.finding must be true or false"
        )

    return case


def main():
    cases = load_cases()

    if not cases:
        raise ValueError("No golden evaluation cases found")

    ids = set()

    for path in cases:
        case = validate_case(path)

        if case["id"] in ids:
            raise ValueError(
                f"Duplicate evaluation case ID: {case['id']}"
            )

        ids.add(case["id"])

    print(f"Validated {len(cases)} golden evaluation cases")
    print("All evaluation cases are valid")


if __name__ == "__main__":
    main()