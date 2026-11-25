"""Serveur MCP qui permet à un agent IA de demander des réservations.

L'agent utilise un compte utilisateur simple : il peut demander et annuler
ses propres réservations, mais jamais les valider ni agir en administrateur.
"""

import logging
import os
import sys
from datetime import datetime

from mcp.server.mcpserver import MCPServer
from mcp.types import ToolAnnotations

from api_client import ApiError, BookingApi

# En mode stdio, la sortie standard sert au protocole MCP : les logs vont sur stderr
logging.basicConfig(stream=sys.stderr, level=logging.INFO)

api = BookingApi(
    base_url=os.environ.get("API_URL", "http://localhost:8080"),
    email=os.environ["MCP_AGENT_EMAIL"],
    password=os.environ["MCP_AGENT_PASSWORD"],
)

mcp = MCPServer(
    name="resource-booking",
    instructions=(
        "Outils pour réserver des machines (serveurs GPU, VM de test). "
        "Une réservation créée reste en attente : seul un administrateur humain peut la valider."
    ),
)


def _verifier_date(valeur: str, nom: str) -> str:
    """Vérifie qu'une date est au format ISO, par exemple 2026-12-01T14:00:00."""
    try:
        return datetime.fromisoformat(valeur).isoformat(timespec="seconds")
    except ValueError:
        raise ApiError(f"{nom} doit être au format AAAA-MM-JJTHH:MM:SS") from None


@mcp.tool(annotations=ToolAnnotations(read_only_hint=True))
def lister_machines() -> list[dict]:
    """Liste les machines réservables, avec leur identifiant, leur nom et leur description."""
    return api.list_resources()


@mcp.tool()
def demander_reservation(machine_id: int, debut: str, fin: str) -> dict:
    """Demande la réservation d'une machine sur un créneau.

    Les dates sont en heure locale, au format AAAA-MM-JJTHH:MM:SS.
    La demande est créée en attente : un administrateur humain doit la valider.
    """
    debut = _verifier_date(debut, "Le début")
    fin = _verifier_date(fin, "La fin")
    return api.create_reservation(machine_id, debut, fin)


@mcp.tool(annotations=ToolAnnotations(read_only_hint=True))
def mes_reservations() -> list[dict]:
    """Liste les réservations du compte de l'agent, avec leur statut."""
    return api.my_reservations()


@mcp.tool(annotations=ToolAnnotations(destructive_hint=True))
def annuler_reservation(reservation_id: int) -> str:
    """Annule une réservation du compte de l'agent (uniquement les siennes)."""
    api.cancel_reservation(reservation_id)
    return f"Réservation {reservation_id} annulée"


if __name__ == "__main__":
    mcp.run()