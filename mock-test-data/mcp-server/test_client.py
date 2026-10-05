import asyncio

from mcp import Client


async def main():
    async with Client("http://127.0.0.1:9000/mcp") as client:

        print("Connected to MCP server")
        print("Protocol:", client.protocol_version)

        result = await client.list_tools()

        print("\nAvailable MCP tools:")
        for tool in result.tools:
            print("-", tool.name)
            print(" ", tool.description)

        print("\nCalling find_test_customer...")

        result = await client.call_tool(
            "find_test_customer",
            {
                "scenario": "mfa_timeout",
                "status": "ACTIVE",
                "mfa_enabled": True
            }
        )

        print("\nMCP tool result object:")
        print(result)

        print("\nMCP content:")
        for item in result.content:
            print(item)

if __name__ == "__main__":
    asyncio.run(main())