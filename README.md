<p align="center">
  <img width="313" height="313" src="https://image.freepik.com/free-vector/gold-star-shape-isolated-white-background-golden-star-icon-gold-star-logo_32511-49.jpg">
</p>


_Twinkle let's you save the current playing Spotify song to a playlist with a single click_

## Installation

1. Clone the project into a folder of your choise (`/opt` by default) 
1. Go into the `twinkle/` folder and run `./install`

### ~/.twinkle 
After installation you have to fill out the `~/.twinkle` configuration file.

#### Access token, refresh token and client ID
These value can be aquired by heading to the [Spotify Web API Authorization guide](https://developer.spotify.com/documentation/general/guides/authorization-guide/), registering an app and following the PKCE flow using the scopes `playlist-modify-private` and `user-read-playback-state`. The refresh token entered in `~/.twinkle` needs to be non-expired as Twinkle will use to to get new access- and refresh tokens automaticly.

#### Playlist ID
This is the Spotify ID of the playlist you want to add your saved songs to. You can find it by selecting a playlist and then _Share -> Copy Spotify URI_ and finally extracting the ID as the last part of the URI. 
For example `spotify:playlist:30IpxqIOXpgFc8PtDOJiLj` means `playlist_id=30IpxqIOXpgFc8PtDOJiLj` 

## Running
Simply run `./twinkle`. You'll head a high piched "twinkle" if the command succeeded. 

_Note! if you installed Twinkle at any other location than `/opt` you need to update the `./twinkle` script accordanly._
