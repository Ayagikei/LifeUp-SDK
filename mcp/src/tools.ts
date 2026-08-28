import { McpServer } from "@modelcontextprotocol/server"
import { z } from "zod"
import { assertCallableMethod } from "./methods.js"
import { readHelp } from "./help.js"

import type { Session } from "./session.js"
import type { LifeUpParamValue } from "./lifeup-url.js"
import { presentCalls, type CallVia } from "./results.js"
import { buildLifeUpUrl } from "./lifeup-url.js"
import { listRequest, presentList, LIST_RESOURCES, type ListArgs } from "./query.js"


async function mutate(
  session: Session,
  method: string,
  params: Record<string, LifeUpParamValue | undefined>,
  via: CallVia = "contentprovider",
) {
  return text(presentCalls(via, await session.requireClient().callApi(method, params, via)))
}

async function queryList(session: Session, args: ListArgs) {
  const request = listRequest(args)
  const raw = await session.requireClient().get(request.path, request.query)
  return text(presentList(args.resource, raw, args))
}


function text(value: unknown) {

  return {
    content: [{ type: "text" as const, text: typeof value === "string" ? value : JSON.stringify(value, null, 2) }],
  }
}

function oneOf<T extends Record<string, unknown>>(obj: T, keys: (keyof T)[]): keyof T {
  const present = keys.filter((key) => obj[key] !== undefined && obj[key] !== null && obj[key] !== "")
  if (present.length !== 1) {
    throw new Error(`Provide exactly one of: ${keys.join(", ")}`)
  }
  return present[0]
}

export function registerTools(server: McpServer, session: Session): void {
  server.registerTool("status", {
    description: "Show LifeUp Cloud connection status, including LifeUp and Cloud versions from GET /info",
    inputSchema: z.object({}),
  }, async () => text(session.status()))

  server.registerTool("discover", {
    description: "Browse LAN for LifeUp Cloud via mDNS (_lifeup._tcp). Auto-connects if exactly one. Empty on corporate Wi-Fi is normal — ask for IP:port from the Cloud app and call connect.",
    inputSchema: z.object({}),
  }, async () => {
    const { discoverCloud } = await import("./discover.js")
    const found = await discoverCloud()
    session.rememberDiscover(found)
    if (found.length === 1) {
      const endpoint = await session.connect({ host: `${found[0].host}:${found[0].port}` })
      return text({ found, endpoint, ...session.status() })
    }
    return text({
      found,
      connected: false,
      hint:
        found.length > 1
          ? "Multiple Clouds. Pass host to connect, like 192.168.1.8:13276."
          : "mDNS found nothing. Common on corporate Wi-Fi (multicast blocked / AP isolation / different VLAN). Ask the user for the IP:port shown in LifeUp Cloud, then connect { host }. Default port 13276.",
    })
  })

  server.registerTool("connect", {
    description: "Connect to LifeUp Cloud. Omit host if discover already found one. host like 192.168.1.8:13276. token is the raw Authorization value if Cloud set one.",
    inputSchema: z.object({
      host: z.string().optional(),
      token: z.string().optional(),
    }),
  }, async ({ host, token }) => {
    const endpoint = await session.connect({ host, token })
    return text({ endpoint, ...session.status() })
  })

  server.registerTool("help", {
    description:
      "Read bundled LifeUp docs. Omit topic for the workflow. api-index then a method name for params. Also basics|discovery|query|tasks|economy|sample_icons|item_structures|qr_scanning|gaps|broadcasts.",
    inputSchema: z.object({
      topic: z.string().optional(),
    }),
  }, async ({ topic }) => text(await readHelp(topic ?? "overview")))

  server.registerTool("list_data", {
    description:
      "Query LifeUp ContentProvider lists (GET). Compact by default. Also coin_records, inventory_records, exp_records, step_records, level_defines, statistics.",
    inputSchema: z.object({
      resource: z.enum(LIST_RESOURCES),
      categoryId: z.number().int().optional(),
      ids: z.array(z.number().int()).optional(),
      gid: z.number().int().optional(),
      offset: z.number().int().optional(),
      limit: z.number().int().optional(),
      detail: z.boolean().optional(),
      timeRangeStart: z.number().int().optional(),
      timeRangeEnd: z.number().int().optional(),
      includeHidden: z.boolean().optional(),
    }),
  }, async (args) => queryList(session, args as ListArgs))

  server.registerTool("list_task_categories", {
    description: "List task categories (compact). Same as list_data resource=task_categories.",
    inputSchema: z.object({
      offset: z.number().int().optional(),
      limit: z.number().int().optional(),
      detail: z.boolean().optional(),
    }),
  }, async (args) => queryList(session, { resource: "task_categories", ...args }))

  server.registerTool("list_tasks", {
    description: "List tasks. Prefer categoryId. Compact; detail=true for full objects.",
    inputSchema: z.object({
      categoryId: z.number().int().optional(),
      offset: z.number().int().optional(),
      limit: z.number().int().optional(),
      detail: z.boolean().optional(),
    }),
  }, async (args) => queryList(session, { resource: "tasks", ...args }))

  server.registerTool("list_history", {
    description: "Task history (server-paged). Query param is gid.",
    inputSchema: z.object({
      offset: z.number().int().optional(),
      limit: z.number().int().optional(),
      gid: z.number().int().optional(),
      detail: z.boolean().optional(),
    }),
  }, async (args) => queryList(session, { resource: "history", ...args }))

  server.registerTool("list_item_categories", {
    description: "List shop categories (compact).",
    inputSchema: z.object({
      offset: z.number().int().optional(),
      limit: z.number().int().optional(),
      detail: z.boolean().optional(),
      includeHidden: z.boolean().optional(),
    }),
  }, async (args) => queryList(session, { resource: "item_categories", ...args }))

  server.registerTool("list_items", {
    description: "List shop items. Use categoryId or ids. Compact by default.",
    inputSchema: z.object({
      categoryId: z.number().int().optional(),
      ids: z.array(z.number().int()).optional(),
      offset: z.number().int().optional(),
      limit: z.number().int().optional(),
      detail: z.boolean().optional(),
    }),
  }, async (args) => queryList(session, { resource: "items", ...args }))

  server.registerTool("list_skills", {
    description: "List attributes/skills (compact).",
    inputSchema: z.object({
      offset: z.number().int().optional(),
      limit: z.number().int().optional(),
      detail: z.boolean().optional(),
    }),
  }, async (args) => queryList(session, { resource: "skills", ...args }))

  server.registerTool("get_coin", {
    description: "Get coin balance (GET /coin).",
    inputSchema: z.object({}),
  }, async () => queryList(session, { resource: "coin" }))

  server.registerTool("get_info", {
    description: "Get LifeUp / Cloud info.",
    inputSchema: z.object({}),
  }, async () => queryList(session, { resource: "info" }))


  server.registerTool("complete_task", {
    description: "Complete a task. Exactly one of id, gid, or name. Count tasks: count + count_set_type (relative|absolute).",
    inputSchema: z.object({
      id: z.number().int().optional(),
      gid: z.number().int().optional(),
      name: z.string().optional(),
      ui: z.boolean().optional(),
      count: z.number().optional(),
      count_set_type: z.enum(["relative", "absolute"]).optional(),
      count_force_sum_up: z.boolean().optional(),
      reward_factor: z.number().optional(),
    }),
  }, async (args) => {
    const key = oneOf(args, ["id", "gid", "name"])
    return mutate(session, "complete", {
      [key]: args[key],
      ui: args.ui ?? false,
      count: args.count,
      count_set_type: args.count_set_type,
      count_force_sum_up: args.count_force_sum_up,
      reward_factor: args.reward_factor,
    })
  })

  server.registerTool("add_task", {
    description: "Create a task. Common fields only. Read calls[].data.task_id / task_gid.",

    inputSchema: z.object({
      todo: z.string(),
      notes: z.string().optional(),
      coin: z.number().int().optional(),
      exp: z.number().int().optional(),
      skills: z.array(z.number().int()).optional(),
      category: z.number().int().optional(),
      frequency: z.number().int().optional(),
      weekdays: z.string().optional(),
      item_id: z.number().int().optional(),
      item_name: z.string().optional(),
    }),
  }, async (args) => mutate(session, "add_task", args))

  server.registerTool("reward", {
    description: "Grant coin, exp, or item. exp needs skills; item needs item_id or item_name.",
    inputSchema: z.object({
      type: z.enum(["coin", "exp", "item"]),
      content: z.string(),
      number: z.number(),
      skills: z.array(z.number().int()).optional(),
      item_id: z.number().int().optional(),
      item_name: z.string().optional(),
    }),
  }, async (args) => {
    if (args.type === "exp" && !args.skills?.length) {
      throw new Error("reward type=exp requires skills")
    }
    if (args.type === "item" && args.item_id == null && !args.item_name) {
      throw new Error("reward type=item requires item_id or item_name")
    }
    return mutate(session, "reward", args)
  })

  server.registerTool("purchase_item", {
    description: "Buy a shop item by id. Read calls[].ok and data.result / data.desc / data.itemId.",

    inputSchema: z.object({
      id: z.number().int(),
      purchase_quantity: z.number().int().optional(),
    }),
  }, async ({ id, purchase_quantity }) => mutate(session, "purchase_item", {
    id,
    purchase_quantity: purchase_quantity ?? 1,
  }))

  const paramSchema = z.record(z.string(), z.union([
    z.string(),
    z.number(),
    z.boolean(),
    z.array(z.union([z.string(), z.number()])),
  ])).optional()

  server.registerTool("call_api", {
    description:
      "Call one lifeup://api method. Default via=contentprovider (returns ids). via=launch opens UI and has no result. Destructive methods need confirm=true.",
    inputSchema: z.object({
      method: z.string(),
      params: paramSchema,
      confirm: z.boolean().optional(),
      via: z.enum(["contentprovider", "launch"]).optional(),
    }),
  }, async ({ method, params, confirm, via }) => {
    assertCallableMethod(method, confirm, params)

    return mutate(session, method, (params ?? {}) as Record<string, LifeUpParamValue>, via ?? "contentprovider")
  })

  server.registerTool("call_api_batch", {
    description:
      "Call several lifeup://api methods in one Cloud request (order preserved). Default via=contentprovider. Any destructive method needs confirm=true on that call or on the batch.",
    inputSchema: z.object({
      calls: z.array(z.object({
        method: z.string(),
        params: paramSchema,
        confirm: z.boolean().optional(),
      })).min(1).max(20),
      via: z.enum(["contentprovider", "launch"]).optional(),
      confirm: z.boolean().optional(),
    }),
  }, async ({ calls, via, confirm }) => {
    for (const item of calls) {
      assertCallableMethod(item.method, item.confirm ?? confirm, item.params)

    }
    const path = via ?? "contentprovider"
    const urls = calls.map((item) => buildLifeUpUrl(item.method, (item.params ?? {}) as Record<string, LifeUpParamValue>))
    return text(presentCalls(path, await session.requireClient().callApis(urls, path)))
  })

  server.registerTool("list_events", {
    description: "Pull LifeUp broadcast events over HTTP GET /events. Default. after= last id.",
    inputSchema: z.object({
      after: z.number().int().optional(),
      limit: z.number().int().optional(),
    }),
  }, async ({ after, limit }) => text(await session.listEvents(after ?? 0, limit ?? 50)))

  server.registerTool("subscribe_events", {
    description: "Open Cloud WebSocket /events (on by default in Cloud 3.0.0+). HTTP list_events still works. on=false closes.",
    inputSchema: z.object({
      after: z.number().int().optional(),
      on: z.boolean().optional(),
    }),
  }, async ({ after, on }) => text(session.setEventSubscription(on ?? true, after ?? 0)))
}

