# MATMAX Signage Android App – GraphQL CMS Integration Guide

## Overview

**MATMAX Signage** is a professional Android digital signage player designed for 24/7 operation in kiosk mode. This document provides detailed, AI-friendly instructions for CMS/backend developers to integrate and manage devices running the MATMAX Signage app using a GraphQL API.

---

## Table of Contents
- [GraphQL Schema Overview](#graphql-schema-overview)
- [Device Pairing Flow](#device-pairing-flow)
- [Device Heartbeat & Status](#device-heartbeat--status)
- [Content Playlist Management](#content-playlist-management)
- [Remote Commands](#remote-commands)
- [Device Configuration Updates](#device-configuration-updates)
- [API Example Queries & Mutations](#api-example-queries--mutations)
- [Error Handling & Status Codes](#error-handling--status-codes)
- [Integration & Extension Tips](#integration--extension-tips)
- [Troubleshooting](#troubleshooting)

---

## GraphQL Schema Overview

The app expects the following GraphQL schema (simplified):

```graphql
# Queries
query GetPlaylist($deviceId: String!) {
  playlist(deviceId: $deviceId) {
    id
    items {
      type   # "WEB", "IMAGE", "VIDEO"
      url
      duration
    }
  }
}

query GetRemoteCommands($deviceId: String!) {
  remoteCommands(deviceId: $deviceId) {
    id
    type   # e.g. "REBOOT", "UPDATE", "SHOW_MESSAGE"
    payload
    issuedAt
  }
}

# Mutations
mutation RegisterDevice($pairingCode: String!, $deviceInfo: String) {
  registerDevice(input: {pairingCode: $pairingCode, deviceInfo: $deviceInfo}) {
    success
    deviceId
    message
  }
}

mutation SendHeartbeat($deviceId: String!, $status: String, $timestamp: String) {
  sendHeartbeat(input: {deviceId: $deviceId, status: $status, timestamp: $timestamp}) {
    success
    message
  }
}

mutation UpdateDeviceConfig($deviceId: String!, $configJson: String!) {
  updateDeviceConfig(input: {deviceId: $deviceId, configJson: $configJson}) {
    success
    message
  }
}
```

---

## Device Pairing Flow

1. **App displays a pairing code** on first boot or after reset.
2. **CMS admin enters the code** in the CMS UI to register the device.
3. **CMS calls the `registerDevice` mutation** with the code and device info.
4. **On success**, the app receives a `deviceId` and stores it for all future requests.

**Example mutation:**
```graphql
mutation {
  registerDevice(input: {pairingCode: "123-456", deviceInfo: "Samsung Tab S6 (Android 13)"}) {
    success
    deviceId
    message
  }
}
```

---

## Device Heartbeat & Status

- The app sends a heartbeat every minute using the `sendHeartbeat` mutation.
- The CMS should use this to track device online/offline status, last seen, and health.
- The `status` field can be used for custom health info (e.g., "ok", "error", "low_battery").

**Example mutation:**
```graphql
mutation {
  sendHeartbeat(input: {deviceId: "abc123", status: "ok", timestamp: "2024-05-01T12:00:00Z"}) {
    success
    message
  }
}
```

---

## Content Playlist Management

- The app fetches its playlist with `getPlaylist(deviceId)`.
- The CMS should return a list of content items (web URLs, images, videos) in the desired order.
- Each item must specify a `type` ("WEB", "IMAGE", "VIDEO"), a `url`, and optionally a `duration` (in seconds).

**Example response:**
```json
{
  "data": {
    "playlist": {
      "id": "pl-001",
      "items": [
        { "type": "WEB", "url": "https://news.example.com", "duration": 30 },
        { "type": "IMAGE", "url": "https://cdn.example.com/banner.jpg", "duration": 15 },
        { "type": "VIDEO", "url": "https://cdn.example.com/spot.mp4", "duration": 60 }
      ]
    }
  }
}
```

---

## Remote Commands

- The app polls for remote commands every minute using `getRemoteCommands(deviceId)`.
- The CMS can return a list of commands for the device (e.g., reboot, update, show message).
- Each command should have an `id`, `type`, `payload` (optional), and `issuedAt` timestamp.
- The app will execute and acknowledge commands as needed (future extension: add command acknowledgment mutation).

**Example response:**
```json
{
  "data": {
    "remoteCommands": [
      { "id": "cmd-001", "type": "REBOOT", "payload": null, "issuedAt": "2024-05-01T12:01:00Z" },
      { "id": "cmd-002", "type": "SHOW_MESSAGE", "payload": "Hello, world!", "issuedAt": "2024-05-01T12:02:00Z" }
    ]
  }
}
```

---

## Device Configuration Updates

- The CMS can update device settings by calling `updateDeviceConfig` with a JSON string.
- The app will apply the new config and respond with success or error.

**Example mutation:**
```graphql
mutation {
  updateDeviceConfig(input: {deviceId: "abc123", configJson: "{\"volume\":80,\"brightness\":60}"}) {
    success
    message
  }
}
```

---

## API Example Queries & Mutations

### Get Playlist
```graphql
query {
  playlist(deviceId: "abc123") {
    id
    items { type url duration }
  }
}
```

### Register Device
```graphql
mutation {
  registerDevice(input: {pairingCode: "123-456", deviceInfo: "Samsung Tab S6"}) {
    success
    deviceId
    message
  }
}
```

### Send Heartbeat
```graphql
mutation {
  sendHeartbeat(input: {deviceId: "abc123", status: "ok", timestamp: "2024-05-01T12:00:00Z"}) {
    success
    message
  }
}
```

### Get Remote Commands
```graphql
query {
  remoteCommands(deviceId: "abc123") {
    id
    type
    payload
    issuedAt
  }
}
```

### Update Device Config
```graphql
mutation {
  updateDeviceConfig(input: {deviceId: "abc123", configJson: "{\"volume\":80}"}) {
    success
    message
  }
}
```

---

## Error Handling & Status Codes

- All mutations return a `success` boolean and a `message` string for error details.
- The app expects clear error messages for failed operations (e.g., invalid pairing code, device not found).
- For queries, return `null` or an empty list if no data is available.

---

## Integration & Extension Tips

- **Device IDs**: Must be unique per device. The app stores the `deviceId` after pairing.
- **Pairing Codes**: Should be short-lived and unique. Expire them after use or timeout.
- **Content URLs**: Must be HTTPS and accessible by the device.
- **Command Types**: Use enums or strings (e.g., "REBOOT", "UPDATE", "SHOW_MESSAGE").
- **Extending the Schema**: Add new fields, queries, or mutations as needed. The app can be updated to support new features (e.g., logs, screenshots, advanced config).
- **Security**: Use authentication and authorization for all API calls. Never expose sensitive info in error messages.

---

## Troubleshooting

- **Device not pairing?**
  - Check the pairing code is valid and not expired.
  - Ensure the CMS calls the correct mutation and returns the `deviceId`.
- **No content displayed?**
  - Ensure the playlist query returns valid items with correct types and URLs.
- **Heartbeats not received?**
  - Check device network connectivity and backend logs.
- **Remote commands not executed?**
  - Ensure commands are returned in the query and are not expired.
- **App crashes or freezes?**
  - Check logs, device resources, and backend responses for errors.

---

## For AI Agents & Automation

- All API interactions are via GraphQL queries/mutations as shown above.
- The schema can be introspected for automation and code generation.
- Use the provided examples as templates for integration and testing.
- Extend the schema and app logic as needed for new CMS features.

---

For further questions or integration support, contact the MATMAX Signage team. 