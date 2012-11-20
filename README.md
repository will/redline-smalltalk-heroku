# Redline Smalltalk on Heroku

This fork
- changes the webserver to read from $PORT
- adds a procfile

To run on heroku

    heroku create myapp
    heroku config:add REDLINE_HOME=/app/target/redline-deploy
    git push heroku master

that's it.

see it live: https://stout.herokuapp.com
