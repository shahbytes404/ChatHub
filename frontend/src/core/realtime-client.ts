import { Client, type IMessage } from "@stomp/stompjs";
import { appConfig } from "./config";
import type { RealtimeEvent } from "./models";

type EventHandler = (event: RealtimeEvent) => void;
type StatusHandler = (connected: boolean) => void;

export class RealtimeClient {
  private client: Client | null = null;
  /**
   * Imagine your ChatHub establishes a WebSocket connection.
   * Initially
   * Frontend  <==========>  Backend
          Connected
    Now imagine your internet connection suddenly drops.

  Your frontend might not immediately know that the backend is unreachable.
  The WebSocket connection may appear active for some time, even though communication is no longer working.
  This is where heartbeats help.
  A heartbeat is a small signal that says:
  "I'm still here, and the connection is working."
   *
   */
  private heartbeatId: number | null = null;
  private eventHandler: EventHandler | null = null;
  private statusHandler: StatusHandler | null = null;

  connect(token: string, onEvent: EventHandler, onStatusChange: StatusHandler) {
    if (this.client?.active) {
      this.eventHandler = onEvent;
      this.statusHandler = onStatusChange;
      return;
    }

    this.eventHandler = onEvent;
    this.statusHandler = onStatusChange;

    console.log("RealtimeClient: Connecting to", appConfig.webSocketBase);

    this.client = new Client({
      brokerURL: appConfig.webSocketBase,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
      heartbeatIncoming: 10000, // This means your frontend expects heartbeat signals from the backend.
      heartbeatOutgoing: 10000, // This means your frontend sends heartbeat signals to the backend.
      onConnect: () => {
        console.log("RealtimeClient: Connected successfully");
        this.statusHandler?.(true);
        this.client?.subscribe("/user/queue/events", (message) =>
          this.capture(message),
        );
        this.startHeartbeat();
      },
      onDisconnect: () => {
        console.log("RealtimeClient: Disconnected");
        this.statusHandler?.(false);
      },
      onStompError: (frame) => {
        console.error("RealtimeClient: STOMP error", frame);
        this.statusHandler?.(false);
      },
      onWebSocketClose: () => {
        console.log("RealtimeClient: WebSocket closed");
        this.statusHandler?.(false);
      },
      onWebSocketError: (error) => {
        console.error("RealtimeClient: WebSocket error", error);
        this.statusHandler?.(false);
      },
    });

    this.client.activate();
  }

  disconnect() {
    this.stopHeartbeat();
    this.statusHandler?.(false);
    this.client?.deactivate();
    this.client = null;
  }

  private capture(message: IMessage) {
    const event = JSON.parse(message.body) as RealtimeEvent;
    this.eventHandler?.(event);
  }

  private startHeartbeat() {
    this.stopHeartbeat();
    this.heartbeatId = window.setInterval(() => {
      if (this.client?.connected) {
        this.client.publish({
          destination: "/app/presence/heartbeat",
          body: "{}",
        });
      }
    }, 10000);
  }

  private stopHeartbeat() {
    if (this.heartbeatId !== null) {
      window.clearInterval(this.heartbeatId);
      this.heartbeatId = null;
    }
  }
}
