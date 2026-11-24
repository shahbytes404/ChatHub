const colorPalette = [
  "#0b7a5c",
  "#1f5fbf",
  "#8a3ffc",
  "#b54708",
  "#b42318",
  "#0f766e",
  "#6d28d9",
  "#0369a1",
];

export const formatShortTime = (value: string) =>
  new Intl.DateTimeFormat([], {
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));

/**
  The purpose is to give each sender a consistent name color in the chat. Every time the same sender ID goes in, the same color comes out. That makes it easier to recognize a sender as you scan the conversation.
 * Picks a palette color for a string key. The same key always gets the same color.
 */
export const randomColor = (key: string) => {
  let hash = 0;
  for (let index = 0; index < key.length; index += 1) {
    hash = (hash << 5) - hash + key.charCodeAt(index);
    hash |= 0;
  }
  return colorPalette[Math.abs(hash) % colorPalette.length];
};
