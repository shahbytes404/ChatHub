import type { ReceiptState } from "../core/models";

export const formatShortTime = (value: string) =>
  new Intl.DateTimeFormat([], {
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));
