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
    'theme': 'neutral'
  }
}%%

graph LR
  subgraph :feature
    :feature:home["home"]
    :feature:artist["artist"]
    :feature:player["player"]
    :feature:folder["folder"]
  end
  :feature:home --> :auth
  :feature:home --> :core
  :feature:home --> :network
  :feature:home --> :uicomponent
  :feature:artist --> :auth
  :feature:artist --> :core
  :feature:artist --> :network
  :feature:artist --> :uicomponent
  :uicomponent --> :core
  :feature:player --> :auth
  :feature:player --> :core
  :feature:player --> :database
  :feature:player --> :network
  :feature:player --> :uicomponent
  :auth --> :database
  :auth --> :uicomponent
  :feature:folder --> :auth
  :feature:folder --> :core
  :feature:folder --> :network
  :feature:folder --> :uicomponent
  :feature:folder --> :feature:player
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
  :database --> :core
```