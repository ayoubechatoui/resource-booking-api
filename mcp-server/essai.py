"""Lance le serveur MCP et appelle ses outils un par un, sans IA, pour vérifier qu'ils marchent."""

import asyncio
import os
import sys

from mcp.client.session import ClientSession
from mcp.client.stdio import StdioServerParameters, stdio_client


async def main() -> None:
    serveur = StdioServerParameters(
        command=sys.executable, args=["server.py"], env=dict(os.environ)
    )
    async with (
        stdio_client(serveur) as (lecture, ecriture),
        ClientSession(lecture, ecriture) as session,
    ):
        await session.initialize()

        outils = await session.list_tools()
        print("Outils exposés :", [outil.name for outil in outils.tools])

        for nom, arguments in [
            ("lister_machines", {}),
            (
                "demander_reservation",
                {
                    "machine_id": 1,
                    "debut": "2027-01-15T14:00:00",
                    "fin": "2027-01-15T16:00:00",
                },
            ),
            (
                "demander_reservation",
                {
                    "machine_id": 1,
                    "debut": "2027-01-15T15:00:00",
                    "fin": "2027-01-15T17:00:00",
                },
            ),
            (
                "demander_reservation",
                {"machine_id": 1, "debut": "demain 14h", "fin": "demain 16h"},
            ),
            ("mes_reservations", {}),
        ]:
            resultat = await session.call_tool(nom, arguments)
            texte = " ".join(getattr(c, "text", "") for c in resultat.content)
            statut = "ERREUR" if resultat.is_error else "OK"
            print(f"\n[{statut}] {nom} {arguments}\n{texte}")


asyncio.run(main())