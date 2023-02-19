<p align="center">
 <img src="https://github.com/Ayagikei/LifeUp-SDK/raw/main/imgs/01.jpg" style="height:600px" />
 <img src="https://github.com/Ayagikei/LifeUp-SDK/raw/main/imgs/02.jpg" style="height:600px" />
</p>
<h2 align="center" padding="100">LifeUp Cloud (SDKs)</h2>

<p align="center">Provide LifeUp SDK, and expose LifeUp APIs as HTTP services!</p>


### Installation

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=net.lifeupapp.lifeup.http">
    <img src="https://img.shields.io/static/v1?labelColor=56595b&color=97db99&logo=google-play&logoColor=ffffff&label=google play&style=for-the-badge&message=get"/>
  </a>


  <a href="https://github.com/Ayagikei/LifeUp-SDK/releases">
    <img src="https://img.shields.io/static/v1?labelColor=56595b&color=a6c6ff&logo=github&logoColor=ffffff&label=Github%20Release&style=for-the-badge&message=get"/>
  </a>
</p>

<br/>

### What's it?

This small extended tool can turn your mobile phone into an HTTP server.
So that you can send API commands (URL Scheme) from the LAN computer to trigger various actions of
the <b>[LifeUp: Gamify To-Do & Habit app](https://play.google.com/store/apps/details?id=net.sarasarasa.lifeup)</b>
.

<b>This can achieve the following effects:</b>

1. Judging the usage time on the computer, the amount of text input, and the drawing time to trigger
   the completion of tasks, rewards, or punishments of the LifeUp application.
2. Implement a simple web page version to create tasks from the computer web page.
3. And everything you can do with computer programming!

### Data Query

<p align="center">
 <img src="https://github.com/Ayagikei/LifeUp-Desktop/raw/master/imgs/cloud.png"/>
</p>


In the 1.1.x version, **it supports querying the complete data list in LifeUp, such as task and
shop item data**, and uses it as the data source of the desktop version.

You can also use these data for secondary development of LifeUp.

### Document

#### LifeUp APIs

[https://docs.lifeupapp.fun/en/#/guide/api](https://docs.lifeupapp.fun/en/#/guide/api)

#### LifeUp SDK

We haven't written the documentation yet, but you can check it out in the source code.

[https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/LifeUpApiDef.kt](https://github.com/Ayagikei/LifeUp-SDK/blob/main/core/src/main/java/net/lifeupapp/lifeup/api/LifeUpApiDef.kt)

#### LifeUp Cloud (HTTP Interface Documentation)

[https://docs.lifeupapp.fun/en/#/guide/api_cloud](https://docs.lifeupapp.fun/en/#/guide/api_cloud)

or check our source code:

[https://github.com/Ayagikei/LifeUp-SDK/blob/main/http/src/main/java/net/lifeupapp/lifeup/http/service/KtorService.kt](https://github.com/Ayagikei/LifeUp-SDK/blob/main/http/src/main/java/net/lifeupapp/lifeup/http/service/KtorService.kt)

<br/>

### Contribution

We're glad to review your pull requests. Please free feel to improve this project.

Please follow our commit message standard. Other than that, there are no special requirements.
