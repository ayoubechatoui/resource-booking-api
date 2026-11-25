"""Petit client HTTP pour l'API de réservation, utilisé par le serveur MCP."""

import httpx
from mcp.server.mcpserver.exceptions import ToolError


class ApiError(ToolError):
    """Erreur prévue, dont le message peut être montré à l'agent.

    Les autres exceptions (bugs, panne réseau) restent masquées par le SDK MCP :
    l'agent ne voit alors qu'un message générique, sans détail interne.
    """


class BookingApi:
    def __init__(self, base_url: str, email: str, password: str):
        self._email = email
        self._password = password
        self._token: str | None = None
        self._http = httpx.Client(base_url=base_url, timeout=10.0)

    def _login(self) -> None:
        reponse = self._http.post(
            "/api/auth/login",
            json={"email": self._email, "password": self._password},
        )
        if reponse.status_code != 200:
            raise ApiError("Connexion du compte agent impossible")
        self._token = reponse.json()["token"]

    def _request(self, method: str, path: str, **kwargs) -> httpx.Response:
        if self._token is None:
            self._login()
        reponse = self._http.request(
            method, path, headers={"Authorization": f"Bearer {self._token}"}, **kwargs
        )
        # Le jeton expire au bout de 60 minutes : on se reconnecte une fois et on réessaie
        if reponse.status_code == 401:
            self._login()
            reponse = self._http.request(
                method,
                path,
                headers={"Authorization": f"Bearer {self._token}"},
                **kwargs,
            )
        if reponse.status_code >= 400:
            raise ApiError(_message(reponse))
        return reponse

    def list_resources(self) -> list[dict]:
        return self._request("GET", "/api/resources").json()

    def create_reservation(self, resource_id: int, start: str, end: str) -> dict:
        corps = {"resourceId": resource_id, "startTime": start, "endTime": end}
        return self._request("POST", "/api/reservations", json=corps).json()

    def my_reservations(self) -> list[dict]:
        return self._request("GET", "/api/reservations/mine").json()

    def cancel_reservation(self, reservation_id: int) -> None:
        self._request("DELETE", f"/api/reservations/{reservation_id}")


def _message(reponse: httpx.Response) -> str:
    """Récupère le message d'erreur de l'API (format ProblemDetail de Spring), ou un message générique."""
    try:
        corps = reponse.json()
    except ValueError:
        corps = {}
    message = corps.get("detail") or corps.get("message")
    if not message:
        return f"Erreur {reponse.status_code} de l'API"
    # Erreurs de validation : l'API précise le champ concerné
    details = corps.get("errors")
    if isinstance(details, dict) and details:
        message += (
            " ("
            + ", ".join(f"{champ} : {texte}" for champ, texte in details.items())
            + ")"
        )
    return message