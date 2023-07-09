<p align="center">
  <img src="https://github.com/erikryverling/twinkle/assets/1917608/de43c6e1-d77b-4449-a01a-87cde46beb31">
</p>


_Twinkle let's you save the current playing Spotify song to a playlist with a single click_

## Installation

1. Clone the project into a folder of your choise (`/opt` by default) 
1. Go into the `twinkle/` folder and run `./install`

### ~/.twinkle 
After installation you have to fill out the `~/.twinkle` configuration file.

#### Access token, refresh token and client ID + client secret hash
These values can be aquired by heading to [Getting started with Web API](https://developer.spotify.com/documentation/web-api/tutorials/getting-started), registering an app and following the _Authorization Code Flow_ guide. The refresh token entered in `~/.twinkle` needs to be non-expiring as Twinkle will use it to get new access tokens automaticly.

#### Playlist ID
This is the Spotify ID of the playlist you want to add your saved songs to. You can find it by selecting a playlist and then _Share -> Copy Spotify URI_ and finally extracting the ID as the last part of the URI. 
For example `spotify:playlist:30IpxqIOXpgFc8PtDOJiLj` means `playlist_id=30IpxqIOXpgFc8PtDOJiLj` 

## Running
Simply run `./twinkle`. You'll head a high piched "twinkle" if the command succeeded. 

_Note! if you installed Twinkle at any other location than `/opt` you need to update the `./twinkle` script accordanly._
