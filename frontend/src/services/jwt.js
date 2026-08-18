export function decodeToken(token){
  const payload = token.split(".")[1];

  const decodePayload = atob(
    payload.replace(/-/g, "+").replace("/_/g", "/")
  );

  return JSON.parse(decodePayload);
}