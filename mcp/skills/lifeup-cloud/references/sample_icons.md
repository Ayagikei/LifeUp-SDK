# Built-in sample icons (`lifeup_sample_*`)

LifeUp ships **208** flat sample icons for shop items, achievements, and categories. Pass the **filename** directly in API calls; the in-app icon picker loads the same set remotely.

## Usage in API / MCP

Pass the filename (with or without `.png`) as `icon`, `set_icon`, or `icon_uri`:

```text
lifeup://api/add_item?name=Coffee break&icon=lifeup_sample_122.png&price=10
lifeup://api/item?id=5&set_icon=lifeup_sample_122
lifeup://api/achievement?name=Collector&icon_uri=lifeup_sample_50.png
```

MCP `call_api` params use the same raw value (do not URL-encode the filename unless it contains reserved characters).

## Catalog

Labels are vision-generated from icon artwork (Chinese + English keywords). Prefer **emoji** for one-off custom icons; use **sample icons** when you want a consistent flat style.

| id | filename | label_zh | label_en | tags |
| --- | --- | --- | --- | --- |
| 1 | lifeup_sample_1.png | 船锚 | Anchor | marine, nautical, sea, stability |
| 2 | lifeup_sample_2.png | 快门 | Camera Shutter | camera, lens, aperture, photography |
| 3 | lifeup_sample_3.png | 向下箭头 | Down Arrow | arrow, down, download, direction |
| 4 | lifeup_sample_4.png | 向上箭头 | Up Arrow | arrow, up, upload, upgrade |
| 5 | lifeup_sample_5.png | 调色盘 | Paint Palette | art, paint, color, design |
| 6 | lifeup_sample_6.png | 柱状图 | Bar Chart | statistics, data, chart, ranking |
| 7 | lifeup_sample_7.png | 满电电池 | Full Battery | battery, power, energy, status |
| 8 | lifeup_sample_8.png | 低电量 | Low Battery | battery, power, warning, low |
| 9 | lifeup_sample_9.png | 自行车 | Bicycle | sports, transport, cycling, fitness |
| 10 | lifeup_sample_10.png | 骑行 | Cycling | sport, bicycle, fitness, outdoor |
| 11 | lifeup_sample_11.png | 方向盘 | Steering Wheel | wheel, transport, direction, steering |
| 12 | lifeup_sample_12.png | 飞艇 | Blimp | transport, flight, airship, travel |
| 13 | lifeup_sample_13.png | 闪电 | Lightning | electricity, energy, power, flash |
| 14 | lifeup_sample_14.png | 炸弹 | Bomb | danger, weapon, explosion, fuse |
| 15 | lifeup_sample_15.png | 笔记本 | Notebook | book, journal, diary, study |
| 16 | lifeup_sample_16.png | 书籍 | Books | reading, study, library, education |
| 17 | lifeup_sample_17.png | 公文包 | Briefcase | work, business, office, career |
| 18 | lifeup_sample_18.png | 灯泡 | Lightbulb | idea, inspiration, light, creativity |
| 19 | lifeup_sample_19.png | 网页 | Web Page | internet, browser, website, content |
| 20 | lifeup_sample_20.png | 绘画工具 | Art Tools | art, pencil, brush, creative |
| 21 | lifeup_sample_21.png | 计算器 | Calculator | math, arithmetic, calculation, study |
| 22 | lifeup_sample_22.png | 日历 | Calendar | date, schedule, time, day |
| 23 | lifeup_sample_23.png | 相机 | Camera | photo, photography, media, capture |
| 24 | lifeup_sample_24.png | 小汽车 | Red Car | car, vehicle, transport, travel |
| 25 | lifeup_sample_25.png | 购物篮 | Shopping Basket | shop, commerce, retail, buy |
| 26 | lifeup_sample_26.png | 车轮 | Car Wheel | tire, automotive, transport, vehicle |
| 27 | lifeup_sample_27.png | 警告 | Warning | alert, caution, error, exclamation |
| 28 | lifeup_sample_28.png | 消息 | Message | chat, dialogue, communication, bubble |
| 29 | lifeup_sample_29.png | 已完成 | Task Done | task, checkmark, complete, success |
| 30 | lifeup_sample_30.png | 圆规 | Drawing Compass | geometry, drafting, design, math |
| 31 | lifeup_sample_31.png | 场记板 | Clapperboard | movie, film, cinema, video |
| 32 | lifeup_sample_32.png | 任务列表 | Task List | clipboard, todo, list, note |
| 33 | lifeup_sample_33.png | 时钟 | Clock | time, schedule, timer, duration |
| 34 | lifeup_sample_34.png | 云朵 | Cloud | weather, sky, storage, sync |
| 35 | lifeup_sample_35.png | 彩色水滴 | Color Drops | droplets, art, palette, creative |
| 36 | lifeup_sample_36.png | 属性环 | Attribute Ring | categories, balance, segments, diversity |
| 37 | lifeup_sample_37.png | 指南针 | Compass | navigation, direction, travel, guide |
| 38 | lifeup_sample_38.png | 笔记 | Note | pencil, paper, writing, document |
| 39 | lifeup_sample_39.png | 显示器 | Monitor | computer, display, screen, tech |
| 40 | lifeup_sample_40.png | 交通锥 | Traffic Cone | caution, construction, warning, road |
| 41 | lifeup_sample_41.png | 通讯录 | Contacts | address book, user, contact, list |
| 42 | lifeup_sample_42.png | 亮度 | Brightness | contrast, display, settings, appearance |
| 43 | lifeup_sample_43.png | 准星 | Crosshair | target, radar, aiming, goal |
| 44 | lifeup_sample_44.png | 信用卡 | Credit Card | payment, finance, banking, money |
| 45 | lifeup_sample_45.png | 裁剪 | Crop Tool | edit, image, resize, selection |
| 46 | lifeup_sample_46.png | 路标 | Signpost | navigation, direction, choice, travel |
| 47 | lifeup_sample_47.png | 轮船 | Ship | boat, nautical, travel, water |
| 48 | lifeup_sample_48.png | 鼠标 | Mouse Cursor | pointer, click, UI, navigation |
| 49 | lifeup_sample_49.png | 禁止 | Prohibited | no entry, warning, stop, forbidden |
| 50 | lifeup_sample_50.png | 代码 | Code | programming, developer, script, tech |
| 51 | lifeup_sample_51.png | 骰子 | Dice | game, random, luck, probability |
| 52 | lifeup_sample_52.png | 文档 | Document | paper, file, text, memo |
| 53 | lifeup_sample_53.png | 物流搬运 | Delivery Cart | logistics, moving, box, hand truck, delivery |
| 54 | lifeup_sample_54.png | 木门 | Wooden Door | door, entrance, home, exit |
| 55 | lifeup_sample_55.png | 云端下载 | Cloud Download | cloud, download, sync, storage |
| 56 | lifeup_sample_56.png | 绘画创作 | Art Easel | art, painting, easel, canvas, creative |
| 57 | lifeup_sample_57.png | 艾特符号 | At Sign | email, contact, mention, communication |
| 58 | lifeup_sample_58.png | 眼睛 | Eye | vision, watch, view, observe |
| 59 | lifeup_sample_59.png | 滴管 | Eyedropper | pipette, color, tool, laboratory |
| 60 | lifeup_sample_60.png | 高跟鞋 | High Heel | shoe, fashion, footwear, elegance |
| 61 | lifeup_sample_61.png | 电影胶片 | Film Reel | film, movie, cinema, media |
| 62 | lifeup_sample_62.png | 胶卷 | Film Roll | film, photography, analog, camera |
| 63 | lifeup_sample_63.png | 红旗 | Red Flag | flag, goal, milestone, achievement |
| 64 | lifeup_sample_64.png | 火焰 | Flame | fire, heat, streak, passion |
| 65 | lifeup_sample_65.png | 护膝 | Knee Brace | health, medical, joint, sports, protection |
| 66 | lifeup_sample_66.png | 花朵 | Flower | plant, nature, bloom, garden |
| 67 | lifeup_sample_67.png | 对焦框 | Focus Frame | scan, focus, camera, viewfinder, qr |
| 68 | lifeup_sample_68.png | 文件夹 | Folder | file, directory, storage, archive |
| 69 | lifeup_sample_69.png | 层叠窗口 | Layer Stack | layer, window, stack, ui, pages |
| 70 | lifeup_sample_70.png | 游戏手柄 | Game Controller | game, controller, gaming, entertainment |
| 71 | lifeup_sample_71.png | 汽油桶 | Gas Can | fuel, gasoline, energy, refuel |
| 72 | lifeup_sample_72.png | 设置齿轮 | Settings Gear | settings, gear, config, tool |
| 73 | lifeup_sample_73.png | 原子 | Atom | science, physics, chemistry, nuclear |
| 74 | lifeup_sample_74.png | 互联网 | Internet Globe | globe, web, network, global |
| 75 | lifeup_sample_75.png | 地球 | Earth Globe | globe, world, map, geography |
| 76 | lifeup_sample_76.png | 导航箭头 | Navigation Arrow | navigation, arrow, direction, pointer |
| 77 | lifeup_sample_77.png | 辐射标志 | Radiation | radiation, nuclear, hazard, warning |
| 78 | lifeup_sample_78.png | 爱心 | Heart | love, favorite, health, like |
| 79 | lifeup_sample_79.png | 直升机 | Helicopter | aviation, transport, flight, travel |
| 80 | lifeup_sample_80.png | 热气球 | Hot Air Balloon | balloon, travel, adventure, sky |
| 81 | lifeup_sample_81.png | 沙漏 | Hourglass | time, timer, countdown, wait |
| 82 | lifeup_sample_82.png | 图片相册 | Photo Gallery | image, photo, gallery, landscape |
| 83 | lifeup_sample_83.png | 公路标志 | Highway Shield | shield, highway, route, sign |
| 84 | lifeup_sample_84.png | 钥匙 | Key | key, unlock, access, security |
| 85 | lifeup_sample_85.png | 键盘 | Keyboard | keyboard, typing, input, computer |
| 86 | lifeup_sample_86.png | 相机镜头 | Camera Lens | camera, lens, photography, optical |
| 87 | lifeup_sample_87.png | 灵感灯泡 | Light Bulb | idea, inspiration, creativity, light |
| 88 | lifeup_sample_88.png | 加载动画 | Loading Spinner | loading, spinner, wait, sun |
| 89 | lifeup_sample_89.png | 地图标记 | Map Pin | location, map, gps, navigation |
| 90 | lifeup_sample_90.png | 挂锁 | Padlock | lock, security, privacy, protection |
| 91 | lifeup_sample_91.png | 魔法棒 | Magic Wand | magic, wand, sparkle, enhance |
| 92 | lifeup_sample_92.png | 放大镜 | Magnifying Glass | search, find, magnify, discovery |
| 93 | lifeup_sample_93.png | 邮件信封 | Mail Envelope | mail, email, message, letter |
| 94 | lifeup_sample_94.png | 折叠地图 | Folded Map | map, navigation, travel, guide |
| 95 | lifeup_sample_95.png | 扩音器 | Megaphone | megaphone, announcement, broadcast, alert |
| 96 | lifeup_sample_96.png | 喇叭 | Bullhorn | megaphone, audio, announcement, sound |
| 97 | lifeup_sample_97.png | 内存卡 | Memory Card | storage, sd card, data, hardware |
| 98 | lifeup_sample_98.png | 合并箭头 | Merge Arrow | merge, combine, converge, flow |
| 99 | lifeup_sample_99.png | 麦克风 | Microphone | microphone, audio, voice, record |
| 100 | lifeup_sample_100.png | 手持话筒 | Handheld Mic | microphone, singing, audio, entertainment |
| 101 | lifeup_sample_101.png | 钞票 | Cash Bills | money, cash, currency, finance |
| 102 | lifeup_sample_102.png | 摩托车 | Motorcycle | motorcycle, transport, vehicle, ride |
| 103 | lifeup_sample_103.png | 音乐音符 | Music Note | music, note, audio, melody |
| 104 | lifeup_sample_104.png | 报纸 | Newspaper | news, paper, media, reading |
| 105 | lifeup_sample_105.png | 油漆刷 | Paintbrush | paint, art, tool, red, creative |
| 106 | lifeup_sample_106.png | 画笔 | Paintbrush | paint, art, tool, drawing, creative |
| 107 | lifeup_sample_107.png | 油漆桶 | Paint Bucket | paint, bucket, diy, red, tool |
| 108 | lifeup_sample_108.png | 油漆滚筒 | Paint Roller | paint, roller, renovation, diy, tool |
| 109 | lifeup_sample_109.png | 降落伞 | Parachute | parachute, sky, adventure, drop, rescue |
| 110 | lifeup_sample_110.png | 铅笔 | Pencil | pencil, write, edit, stationery, school |
| 111 | lifeup_sample_111.png | 电话 | Phone | phone, call, communication, contact |
| 112 | lifeup_sample_112.png | 饼图统计 | Pie Chart | chart, statistics, data, analytics, report |
| 113 | lifeup_sample_113.png | 图钉 | Pushpin | pin, mark, fixed, office, stationery |
| 114 | lifeup_sample_114.png | 地图钉 | Map Pin | pin, location, mark, map, red |
| 115 | lifeup_sample_115.png | 飞机 | Airplane | plane, aviation, travel, flight, transport |
| 116 | lifeup_sample_116.png | 播放 | Play | play, media, video, start, button |
| 117 | lifeup_sample_117.png | 电源插头 | Power Plug | plug, power, electricity, energy, connect |
| 118 | lifeup_sample_118.png | 拍立得 | Instant Photo | photo, polaroid, memory, image, gallery |
| 119 | lifeup_sample_119.png | 拍立得相机 | Instant Camera | camera, photography, polaroid, photo, device |
| 120 | lifeup_sample_120.png | 相册 | Photo Album | photos, gallery, album, images, memory |
| 121 | lifeup_sample_121.png | 电源 | Power | power, shutdown, toggle, button, control |
| 122 | lifeup_sample_122.png | 礼盒 | Gift Box | gift, reward, celebration, box, present |
| 123 | lifeup_sample_123.png | 用户头像 | User Profile | user, profile, account, avatar, person |
| 124 | lifeup_sample_124.png | 引用 | Quote | quote, citation, text, motto, dialogue |
| 125 | lifeup_sample_125.png | 终点旗 | Finish Flag | flag, racing, finish, goal, competition |
| 126 | lifeup_sample_126.png | 收音机 | Radio | radio, audio, broadcast, music, retro |
| 127 | lifeup_sample_127.png | 信号塔 | Signal Tower | signal, tower, wifi, broadcast, network |
| 128 | lifeup_sample_128.png | 彩虹 | Rainbow | rainbow, color, weather, hope, arc |
| 129 | lifeup_sample_129.png | 循环利用 | Recycle | recycle, eco, environment, green, loop |
| 130 | lifeup_sample_130.png | 三环互联 | Interlocked Rings | rings, connection, synergy, link, teamwork |
| 131 | lifeup_sample_131.png | 书签 | Bookmark | bookmark, favorite, flag, save, reading |
| 132 | lifeup_sample_132.png | 路障 | Road Barrier | barrier, construction, block, warning, road |
| 133 | lifeup_sample_133.png | 火箭发射 | Rocket Launch | rocket, launch, space, start, ambition |
| 134 | lifeup_sample_134.png | 三角尺 | Set Square | ruler, math, geometry, stationery, measure |
| 135 | lifeup_sample_135.png | 步行 | Walking | walk, exercise, fitness, activity, health |
| 136 | lifeup_sample_136.png | 帆船 | Sailboat | sailboat, sea, travel, sailing, boat |
| 137 | lifeup_sample_137.png | 校车 | School Bus | bus, transport, school, vehicle, travel |
| 138 | lifeup_sample_138.png | 剪刀 | Scissors | scissors, cut, tool, craft, office |
| 139 | lifeup_sample_139.png | 踏板车 | Scooter | scooter, moped, vehicle, ride, transport |
| 140 | lifeup_sample_140.png | 盾牌 | Shield | shield, defense, protection, guard, security |
| 141 | lifeup_sample_141.png | 计时器 | Timer | timer, stopwatch, time, countdown, clock |
| 142 | lifeup_sample_142.png | 调节滑块 | Sliders | sliders, settings, adjust, control, mix |
| 143 | lifeup_sample_143.png | 船舵 | Ship Wheel | helm, nautical, navigation, sea, steering |
| 144 | lifeup_sample_144.png | 脚印 | Footprints | footprints, steps, walk, progress, track |
| 145 | lifeup_sample_145.png | 价格标签 | Price Tag | tag, price, shopping, label, store |
| 146 | lifeup_sample_146.png | 滑板 | Skateboard | skateboard, sport, ride, youth, outdoor |
| 147 | lifeup_sample_147.png | 数码相机 | Camera | camera, photography, photo, device, shoot |
| 148 | lifeup_sample_148.png | 智能手机 | Smartphone | phone, mobile, device, screen, tech |
| 149 | lifeup_sample_149.png | 航天飞机 | Space Shuttle | shuttle, space, launch, rocket, exploration |
| 150 | lifeup_sample_150.png | 音量 | Volume | volume, sound, audio, speaker, media |
| 151 | lifeup_sample_151.png | 速度表 | Speedometer | speedometer, gauge, speed, dashboard, meter |
| 152 | lifeup_sample_152.png | 喷漆罐 | Spray Can | spray, paint, art, graffiti, tool |
| 153 | lifeup_sample_153.png | 多层堆叠 | Stacked Layers | layers, stack, levels, data, architecture |
| 154 | lifeup_sample_154.png | 收藏星 | Star | star, favorite, reward, achievement, badge |
| 155 | lifeup_sample_155.png | 方向盘 | Steering Wheel | steering, car, drive, vehicle, control |
| 156 | lifeup_sample_156.png | 请勿打扰 | Do Not Disturb | block, stop, restricted, quiet, sign |
| 157 | lifeup_sample_157.png | 音箱 | Speaker | audio, music, sound, electronics |
| 158 | lifeup_sample_158.png | 潜水艇 | Submarine | vehicle, ocean, underwater, exploration |
| 159 | lifeup_sample_159.png | 救生圈 | Lifebuoy | safety, rescue, support, help |
| 160 | lifeup_sample_160.png | 色卡 | Color Swatches | color, design, theme, art |
| 161 | lifeup_sample_161.png | 平板电脑 | Tablet | device, electronics, screen, mobile |
| 162 | lifeup_sample_162.png | 飞机 | Airplane | travel, flight, transport, aviation |
| 163 | lifeup_sample_163.png | 靶心 | Bullseye | target, goal, achievement, focus |
| 164 | lifeup_sample_164.png | 出租车 | Taxi | transportation, vehicle, travel, street |
| 165 | lifeup_sample_165.png | 公文包 | Briefcase | business, work, office, career |
| 166 | lifeup_sample_166.png | 工具 | Tools | maintenance, repair, wrench, settings |
| 167 | lifeup_sample_167.png | 拖拉机 | Tractor | agriculture, farm, vehicle, rural |
| 168 | lifeup_sample_168.png | 红绿灯 | Traffic Light | traffic, signal, transport, rules |
| 169 | lifeup_sample_169.png | 地铁 | Subway | transport, commute, train, railway |
| 170 | lifeup_sample_170.png | 行囊 | Bindle | travel, adventure, journey, luggage |
| 171 | lifeup_sample_171.png | 趋势图 | Trend Chart | chart, statistics, data, progress |
| 172 | lifeup_sample_172.png | 三脚架 | Tripod | photography, camera, equipment, stand |
| 173 | lifeup_sample_173.png | 冠军奖杯 | Gold Trophy | trophy, award, achievement, victory |
| 174 | lifeup_sample_174.png | 快递货车 | Delivery Truck | truck, delivery, shipping, logistics |
| 175 | lifeup_sample_175.png | 复古电视 | Retro TV | television, monitor, screen, media |
| 176 | lifeup_sample_176.png | 字体 | Font | font, text, typography, settings |
| 177 | lifeup_sample_177.png | 飞碟 | UFO | space, alien, sci-fi, craft |
| 178 | lifeup_sample_178.png | 雨伞 | Umbrella | rain, weather, protection, accessory |
| 179 | lifeup_sample_179.png | 独轮车 | Unicycle | sport, balance, transport, circus |
| 180 | lifeup_sample_180.png | 开锁 | Open Lock | security, unlock, access, open |
| 181 | lifeup_sample_181.png | 云上传 | Cloud Upload | cloud, upload, sync, backup |
| 182 | lifeup_sample_182.png | 胶片 | Film Strip | film, cinema, movie, video |
| 183 | lifeup_sample_183.png | 电影机 | Film Camera | film, cinema, video, projector |
| 184 | lifeup_sample_184.png | 摄像机 | Camcorder | video, record, camera, media |
| 185 | lifeup_sample_185.png | 均衡器 | Equalizer | audio, sound, music, levels |
| 186 | lifeup_sample_186.png | 水滴 | Water Drop | water, liquid, hydration |
| 187 | lifeup_sample_187.png | 晴间多云 | Partly Cloudy | weather, sun, cloud, sky |
| 188 | lifeup_sample_188.png | 风向袋 | Windsock | wind, weather, direction, meteorology |
| 189 | lifeup_sample_189.png | 蜿蜒上升 | Winding Up | progress, growth, path, upward |
| 190 | lifeup_sample_190.png | 取消勾选 | Cancelled | cancel, reject, failure, checkbox |
| 191 | lifeup_sample_191.png | 放大 | Zoom In | zoom, search, magnify, plus |
| 192 | lifeup_sample_192.png | 缩小 | Zoom Out | zoom, search, minus, interface |
| 193 | lifeup_sample_193.png | 热茶 | Hot Tea | tea, drink, beverage, hot |
| 194 | lifeup_sample_194.png | 服饰 | Clothing | shirt, bag, fashion, apparel |
| 195 | lifeup_sample_195.png | 心愿单 | Wishlist | shopping, cart, heart, favorite |
| 196 | lifeup_sample_196.png | 票券 | Tickets | ticket, event, coupon, entertainment |
| 197 | lifeup_sample_197.png | 化妆品 | Cosmetics | makeup, lipstick, beauty, pink |
| 198 | lifeup_sample_198.png | 认证勋章 | Award Medal | medal, achievement, completion, certification |
| 199 | lifeup_sample_199.png | 山水 | Landscape | nature, mountains, scenery, outdoors |
| 200 | lifeup_sample_200.png | 阅读 | Reading | book, education, learning, study |
| 201 | lifeup_sample_201.png | 礼物 | Gift Box | gift, reward, present, celebration |
| 202 | lifeup_sample_202.png | 钻石 | Diamond | gem, treasure, reward, premium |
| 203 | lifeup_sample_203.png | 烤鸡 | Roast Chicken | food, feast, dinner, poultry |
| 204 | lifeup_sample_204.png | 羽毛球 | Shuttlecock | sport, badminton, fitness, equipment |
| 205 | lifeup_sample_205.png | 播放视频 | Watch Video | video, play, entertainment, television |
| 206 | lifeup_sample_206.png | 游戏手柄 | Game Controller | gaming, controller, play, entertainment |
| 207 | lifeup_sample_207.png | 音乐 | Music | music, note, record, entertainment |
| 208 | lifeup_sample_208.png | 移动设备 | Mobile Devices | device, mobile, tablet, smartphone |
