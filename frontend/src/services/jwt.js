export function decodeToken(token){
  try {
    const payload = token.split(".")[1];

    const decodePayload = atob(
      payload.replace(/-/g, "+").replace(/_/g, "/")
    );

    return JSON.parse(decodePayload);
  } catch {
    return null;
  }
}

export function isTokenExpired(token){
  try {
    const decoded = decodeToken(token);
    if (!decoded || !decoded.exp) return true;
    return Date.now() >= decoded.exp * 1000;
  } catch {
    return true;
  }
}