## Android client for [Swing Music](https://github.com/swingmx/swingmusic)

This project is currently in its early development stages as the [Swing Music team](https://github.com/orgs/swingmx/people) is diligently working to deliver a beta version as soon as possible.

<table>
  <tr>
    <td>
      <img src="https://github.com/swingmx/android/assets/54077752/0344f6f9-dd70-4a4f-adf9-2a883758af28" width="320" alt="image" />
    </td>
    <td>
      <img src="https://github.com/swingmx/android/assets/54077752/59649546-295b-4e40-8e3e-8e03dd1f7dd7" width="320" alt="image" />
    </td>
  </tr>
  
  <tr>
    <td>
      <img src="https://github.com/swingmx/android/assets/54077752/de0abb9d-95ed-4e2f-91ff-20dbf5288809" width="320" alt="image" />
    </td>
    <td>
      <img src="" width="320" alt="" />
    </td>
  </tr>
</table>

[![wakatime](https://wakatime.com/badge/user/99206146-a1fc-4be5-adc8-c2351f27ecef/project/018e7aae-f9e9-42e9-99e1-fc381580884d.svg)](https://wakatime.com/badge/user/99206146-a1fc-4be5-adc8-c2351f27ecef/project/018e7aae-f9e9-42e9-99e1-fc381580884d)

### Module Graph

```mermaid
%%{
  init: {
    'theme': 'dark'
  }
}%%

graph LR
  subgraph :feature
    :feature:home["home"]
    :feature:artist["artist"]
    :feature:player["player"]
    :feature:common["common"]
    :feature:folder["folder"]
    :feature:album["album"]
  end
  :feature:home --> :auth
  :feature:home --> :core
  :feature:home --> :network
  :feature:home --> :uicomponent
  :feature:artist --> :auth
  :feature:artist --> :core
  :feature:artist --> :network
  :feature:artist --> :uicomponent
  :feature:artist --> :feature:player
  :feature:artist --> :feature:common
  :uicomponent --> :core
  :feature:player --> :auth
  :feature:player --> :core
  :feature:player --> :database
  :feature:player --> :network
  :feature:player --> :uicomponent
  :auth --> :database
  :auth --> :core
  :auth --> :uicomponent
  :feature:folder --> :auth
  :feature:folder --> :core
  :feature:folder --> :network
  :feature:folder --> :uicomponent
  :feature:folder --> :feature:player
  :feature:folder --> :feature:album
  :network --> :auth
  :network --> :core
  :network --> :database
  :app --> :auth
  :app --> :core
  :app --> :network
  :app --> :uicomponent
  :app --> :feature:home
  :app --> :feature:folder
  :app --> :feature:player
  :app --> :feature:artist
  :app --> :feature:album
  :app --> :feature:common
  :database --> :core
  :feature:album --> :auth
  :feature:album --> :core
  :feature:album --> :network
  :feature:album --> :uicomponent
  :feature:album --> :feature:player
  :feature:album --> :feature:artist

classDef android-library fill:#3BD482,stroke:#fff,stroke-width:2px,color:#fff;
classDef android-application fill:#2C4162,stroke:#fff,stroke-width:2px,color:#fff;
class :feature:home android-library
class :auth android-library
class :core android-library
class :network android-library
class :uicomponent android-library
class :feature:artist android-library
class :feature:player android-library
class :feature:common android-library
class :database android-library
class :feature:folder android-library
class :feature:album android-library
class :app android-application

```