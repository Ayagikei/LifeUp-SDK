# feeling

Source: lifeup-wiki `docs/en/guide/api.md` (may lag).

**Method name:** feeling

**Description:** It is used to create or update records of feelings.

**Example:**

- Create a new record of feeling: [lifeup://api/feeling?content=Happy&time=1633036800](lifeup://api/feeling?content=Happy&time=1633036800)
- Update an existing record of feeling and mark it as a favorite: [lifeup://api/feeling?id=1&is_favorite=true](lifeup://api/feeling?id=1&is_favorite=true)
- Delete a feeling: [lifeup://api/feeling?id=1&delete=true](lifeup://api/feeling?id=1&delete=true)

| Parameter            | Meaning           | Type                               | Example           | Required | Notes                                                                                                                                                                                                                                        |
| -------------------- | ----------------- | ---------------------------------- | ----------------- | -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| id                   | Feeling Record ID | Number greater than 0              | 1                 | No       | If provided, the method tries to update a specific record. Required when deleting.                                                                                                                                                           |
| content              | Content           | Any text                           | Happy             | No       | Used for creating a new record or updating the content of an existing one                                                                                                                                                                    |
| time                 | Timestamp         | Unix timestamp                     | 1633036800        | No       | The time of the record, defaults to current time                                                                                                                                                                                             |
| is_favorite          | Favorite Flag     | true or false                      | true              | No       | Marks the record as a favorite or not                                                                                                                                                                                                        |
| delete               | Delete            | true or false                      | true              | No       | v1.105.1+. Soft-deletes the feeling the same way as the app (attachments are removed).                                                                                                                                                       |
| relate_type          | Relation Type     | Number between 0 and 3             | 1                 | No       | Specifies the type of relation associated with the record:<br/>0: Task<br/>1: Custom Achievement<br/>2: No relation<br/>3: Item usage                                                                                                        |
| relate_id            | Related ID        | Number greater than 0              | 2                 | No       | Specifies the ID of the related item:<br/>When relate_type is 0: represents task ID<br/>When relate_type is 1: represents achievement ID<br/>When relate_type is 3: represents item ID<br/>When relate_type is 2: no ID needed                |
| usage_count          | Usage count       | Integer greater than 1             | 1                 | No       | Only valid when relate_type is 3 (Item usage), records the usage count of the item.                                                                                                                                                          |
| image_uris           | Image URIs        | List of URI strings                |                   | No       | Supports local file URIs (file://) or remote web images (http/https). Supports arrays (e.g., &image_uris=uri1&image_uris=uri2). |
| image_uris_update_mode | Update Mode       | APPEND or REPLACE | REPLACE           | No       | Only valid when updating an existing record and providing image_uris.<br/>APPEND: Appends to existing images.<br/>REPLACE: Replaces existing images (default).                                                               |
    
**Note:**

1. If the `id` parameter is provided, the method attempts to update the corresponding record of feeling. An exception is thrown if no matching record is found.
2. If `id` is not provided, but `content` is, the method will create a new record of feeling.

<br/>
