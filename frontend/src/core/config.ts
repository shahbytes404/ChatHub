const trimTrailingSlash = (value: string) => value.replace(/\/+$/, '');

const defaultApiBase = 
    typeof window !== 'undefined'
        ? `${window.location.origin}/api`
        : 'http://localhost:8080/api';

const defaultWebSocketBase = 
    typeof window !== 'undefined'
        ? `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.hostname}:8080/ws`
        : 'ws://localhost:8080/ws';

const apiBase = trimTrailingSlash(
    import.meta.env.VITE_API_URL?.trim() || defaultApiBase,
)

const webSocketBase = trimTrailingSlash(
    import.meta.env.VITE_WS_URL?.trim() || defaultWebSocketBase
)

export const appConfig = {
    apiBase,
    webSocketBase
}