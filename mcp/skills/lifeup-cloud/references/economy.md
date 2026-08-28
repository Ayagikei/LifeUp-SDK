# Economy

## reward / penalty

`type` + `content` + `number` (+ `silent`):

- `coin`
- `exp` requires `skills[]`
- `item` requires `item_id` or `item_name`

`penalty` is the same shape via `call_api`.

## purchase_item

Wiki: `id` **or** `name`, plus optional `purchase_quantity` (default 1).  
First-slice tool currently takes `id` only; use `call_api` for `name`.

## coins / ATM / tomatoes

- `get_coin` → `data.value` (also `call_api` `query` `key=coin`)
- `edit_coin` absolute `coin`, needs `confirm: true`
- `deposit` / `withdraw` `amount` (balance-checked)
- `shop_settings` `key`+`value` (atm_interest, credit_interest, line_of_credit, discount_rate_for_returning, atm_balance)
- `tomato` `number` + `action` (increase|decrease|set)

