from mcp.server import MCPServer
import json
from urllib.parse import urlencode
from urllib.request import urlopen

mcp = MCPServer("ciam-test-data")


@mcp.tool()
def find_test_customer(
    scenario: str,
    status: str = "ACTIVE",
    mfa_enabled: bool | None = None,
) -> dict:
    """
    Find a synthetic CIAM test customer suitable for a test scenario.
    """

    params = {
        "scenario": scenario,
        "status": status
    }

    if mfa_enabled is not None:
        params["mfaEnabled"] = str(mfa_enabled).lower()

    url = (
        "http://127.0.0.1:8090/test-customers?"
        + urlencode(params)
    )

    with urlopen(url, timeout=5) as response:
        return json.loads(
            response.read().decode("utf-8")
        )


if __name__ == "__main__":
    mcp.run(
        transport="streamable-http",
        host="127.0.0.1",
        port=9000
    )