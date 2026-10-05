import json
from pathlib import Path

REGISTRY_FILE = Path(__file__).parent / "registry.json"


def load_registry():
    with open(REGISTRY_FILE, "r", encoding="utf-8") as file:
        return json.load(file)


def check_mcp(name, version):
    registry = load_registry()

    for mcp in registry["approved_mcps"]:

        if mcp["name"] != name:
            continue

        if mcp["status"] != "APPROVED":
            return {
                "decision": "DENY",
                "reason": "MCP is not currently approved",
                "mcp": name,
                "version": version
            }

        if mcp["approved_version"] != version:
            return {
                "decision": "DENY",
                "reason": "MCP version is not approved",
                "mcp": name,
                "version": version,
                "approved_version": mcp["approved_version"]
            }

        if mcp["risk"] != "LOW":
            return {
                "decision": "DENY",
                "reason": "MCP risk level requires additional approval",
                "mcp": name,
                "risk": mcp["risk"]
            }

        if mcp["production_access"] is True:
            return {
                "decision": "DENY",
                "reason": "Production access is not permitted by this policy",
                "mcp": name
            }

        if mcp["write_access"] is True:
            return {
                "decision": "DENY",
                "reason": "Write access is not permitted by this policy",
                "mcp": name
            }

        return {
            "decision": "ALLOW",
            "reason": "MCP satisfies enterprise registration policy",
            "mcp": name,
            "version": version,
            "risk": mcp["risk"]
        }

    return {
        "decision": "DENY",
        "reason": "MCP is not present in enterprise registry",
        "mcp": name,
        "version": version
    }


if __name__ == "__main__":
    import sys

    if len(sys.argv) != 3:
        print("Usage: python policy.py <mcp-name> <version>")
        raise SystemExit(1)

    name = sys.argv[1]
    version = sys.argv[2]

    print(json.dumps(check_mcp(name, version), indent=2))
