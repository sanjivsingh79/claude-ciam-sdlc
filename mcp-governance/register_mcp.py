import json
import sys
from pathlib import Path

from policy import check_mcp

REGISTRATIONS_FILE = Path(__file__).parent / "registrations.json"


def load_registrations():
    if not REGISTRATIONS_FILE.exists():
        return []

    with open(REGISTRATIONS_FILE, "r", encoding="utf-8") as file:
        return json.load(file)


def save_registrations(registrations):
    with open(REGISTRATIONS_FILE, "w", encoding="utf-8") as file:
        json.dump(registrations, file, indent=2)


def register_mcp(name, version):
    decision = check_mcp(name, version)

    if decision["decision"] != "ALLOW":
        print(json.dumps({
            "registration": "BLOCKED",
            **decision
        }, indent=2))
        return False

    registrations = load_registrations()

    registration = {
        "name": name,
        "version": version,
        "status": "REGISTERED"
    }

    registrations.append(registration)
    save_registrations(registrations)

    print(json.dumps({
        "registration": "SUCCESS",
        **registration
    }, indent=2))

    return True


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python register_mcp.py <mcp-name> <version>")
        raise SystemExit(1)

    register_mcp(sys.argv[1], sys.argv[2])
