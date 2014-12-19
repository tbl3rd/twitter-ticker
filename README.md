# twitter-ticker

A Twitter ticker demo.

## Usage

Here's how to build and deploy the demo on a Linux machine
assuming it has the basic Java infrastructure installed.

```
tbl3rd@ownlife ~ # git clone https://github.com/tbl3rd/twitter-ticker.git
Initialized empty Git repository in /home/tbl3rd/twitter-ticker/.git/
tbl3rd@ownlife ~ # cd twitter-ticker
tbl3rd@ownlife ~/twitter-ticker # ls
README.md  doc	project.clj  src  test	ticker
tbl3rd@ownlife ~/twitter-ticker # curl https://raw.github.com/technomancy/leiningen/preview/bin/lein > ./lein
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  9575  100  9575    0     0  20628      0 --:--:-- --:--:-- --:--:-- 63410
tbl3rd@ownlife ~/twitter-ticker # chmod 775 ./lein
tbl3rd@ownlife ~/twitter-ticker # ./lein
Leiningen is a tool for working with Clojure projects.

Several tasks are available:
check               Check syntax and warn on reflection.
classpath           Write the classpath of the current project to output-file.
clean               Remove all files from project's target-path.
cljsbuild           Compile ClojureScript source into a JavaScript file.
compile             Compile Clojure source into .class files.
deploy              Build jar and deploy to remote repository.
deps                Show details about dependencies.
do                  Higher-order task to perform other tasks in succession.
help                Display a list of tasks or help for a given task.
install             Install current project to the local repository.
jar                 Package up all the project's files into a jar file.
javac               Compile Java source files.
new                 Generate project scaffolding based on a template.
plugin              DEPRECATED. Please use the :user profile instead.
pom                 Write a pom.xml file to disk for Maven interoperability.
repl                Start a repl session either with the current project or standalone.
retest              Run only the test namespaces which failed last time around.
run                 Run the project's -main function.
search              Search remote maven repositories for matching jars.
show-profiles       List all available profiles or display one if given an argument.
test                Run the project's tests.
trampoline          Run a task without nesting the project's JVM inside Leiningen's.
uberjar             Package up the project files and all dependencies into a jar file.
upgrade             Upgrade Leiningen to specified version or latest stable.
version             Print version for Leiningen and the current JVM.
with-profile        Apply the given task with the profile(s) specified.

Run lein help $TASK for details.

Aliases:

See also: readme, faq, tutorial, news, sample, profiles,
deploying and copying.
tbl3rd@ownlife ~/twitter-ticker # ./lein uberjar
Compiling ticker.server
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Compiling ClojureScript.
Compiling "ticker/js/follow.js" from "src/cljs/follow"...
... stuff ...
Including jetty-continuation-7.6.1.v20120215.jar
Created /home/tbl3rd/twitter-ticker/target/ticker-0.1.0-SNAPSHOT-standalone.jar
tbl3rd@ownlife ~/twitter-ticker # java -jar ./target/ticker-0.1.0-SNAPSHOT-standalone.jar 8080
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Starting server...
2012-10-03 13:43:19.275:INFO:oejs.Server:jetty-7.x.y-SNAPSHOT
2012-10-03 13:43:19.379:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:8080
Server started on port [8080].
You can view the site at http://localhost:8080
Serving ticker/css/feed.css
Serving ticker/js/feed.js
In :get /ticker/follow
Serving ticker/css/follow.css
Serving ticker/js/follow.js
In :post /ticker/follow/add
... On adding @barackobama on the /follow page ...
Following: @barackobama
... Start seeing tweets on the /feed page ...
restart-filter-stream-in-agent
In :get /ticker/follow
... On hitting a Stop button on the /follow page ...
follow-stop-user {:id 813286, :avatar http://a0.twimg.com/profile_images/2325704772/wrrmef61i6jl91kwkmzq_normal.png, :name Barack Obama, :screen @BarackObama}
Serving ticker/js/follow.js
Serving ticker/css/follow.css
...
tbl3rd@ownlife ~/demo #
```

You can specify a different port on the command line.  For example,
the following command tells the demo server to use 8080 instead of
port 80.

    java -jar .../ticker-0.1.0-SNAPSHOT-standalone.jar 8080

The default port is 80.

Open http://localhost:8080/ticker/feed to get the Twitter feed overlay.
Open http://localhost:8080/ticker/follow to follow some Twitter users.

You may see something like the following response in the console when
adding the first @ScreenName in the /follow form.

```
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<title>Error 401 UNAUTHORIZED</title>
</head>
<body>
<h2>HTTP ERROR: 401</h2>
<p>Problem accessing /1/statuses/sample.json. Reason:
<pre>    UNAUTHORIZED</pre></p>
```

That means there is an OAuth validation problem between the Twitter
client running in the demo server and the Twitter API service.  The
usual problem is that the demo's server doesn't know what time it is.
Fix that with 'su date ...' or whatever and restart the demo.

This is how to set up the RFB source.  First, start the server running
as root.  You need to run as root to serve the default HTTP port 80.

```
tbl3rd@engsoc ~/engsoc/twitter-ticker # lein uberjar
lein uberjar
Compiling ClojureScript.
Created /home/tbl3rd/engsoc/twitter-ticker/target/ticker-0.1.0-SNAPSHOT.jar
Including ticker-0.1.0-SNAPSHOT.jar
...
Including jetty-continuation-7.6.1.v20120215.jar
Created /home/tbl3rd/engsoc/twitter-ticker/target/ticker-0.1.0-SNAPSHOT-standalone.jar
tbl3rd@engsoc ~/engsoc/twitter-ticker # su
su
Password:

root@engsoc /home/tbl3rd/engsoc/twitter-ticker # java -jar ./target/ticker-0.1.0-SNAPSHOT-standalone.jar &
java -jar ./target/ticker-0.1.0-SNAPSHOT-standalone.jar &
[1] 9522
root@engsoc /home/tbl3rd/engsoc/twitter-ticker # SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Starting server...
2012-10-03 18:24:14.908:INFO:oejs.Server:jetty-7.x.y-SNAPSHOT
2012-10-03 18:24:14.970:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:80
Server started on port [80].
You can view the site at http://localhost:80

root@engsoc /home/tbl3rd/engsoc/twitter-ticker # exit
exit
tbl3rd@engsoc ~/engsoc/twitter-ticker #
```

Then exit back to run again as whomever, start an Xvnc server running
Google Chrome in "kiosk mode" on the feed page.  To do that set up
your ~/.vnc/xstartup file like this.

```
tbl3rd@engsoc ~/engsoc/twitter-ticker # cat ~/.vnc/xstartup
#!/bin/sh
/usr/bin/google-chrome --kiosk http://localhost/ticker/feed
tbl3rd@engsoc ~/engsoc/twitter-ticker #
```

Then start up an Xvnc server on your ~/.vnc/xstartup file like this.

```
tbl3rd@engsoc ~/engsoc/twitter-ticker # vncserver :0 -desktop X -geometry 1280x720 -depth 32 -alwaysshared -securitytypes none

New 'engsoc.tbl3rd.com:0 (tbl3rd)' desktop is engsoc.tbl3rd.com:0

Starting applications specified in /home/tbl3rd/.vnc/xstartup
Log file is /home/tbl3rd/.vnc/engsoc.tbl3rd.com:0.log

tbl3rd@engsoc ~/engsoc/twitter-ticker #
```

There is a ~/bin/ticker-demo to help me remember all this junk.

```
tbl3rd@engsoc ~/engsoc/twitter-ticker # cat ~/bin/ticker-demo
#!/bin/sh
#
# As root
#
# java -jar ~/engsoc/twitter-ticker/target/ticker-0.1.0-SNAPSHOT-standalone.jar
#
# Then with the following in ~/.vnc/xstartup ...
#
#       tbl3rd@engsoc ~/engsoc/twitter-ticker # cat ~/.vnc/xstartup
#       #!/bin/sh
#       /usr/bin/google-chrome --kiosk http://localhost/ticker/feed
#       tbl3rd@engsoc ~/engsoc/twitter-ticker #
#
vncserver :0 -desktop X -geometry 1280x720 -depth 32 -alwaysshared -securitytypes none
echo kill $(cat ~/.vnc/engsoc*:0.pid)
tbl3rd@engsoc ~/engsoc/twitter-ticker #
```

Now you should be able to start up a vncviewer to check that
everything is running as expected, then just kill it or put it in the
background.

```
tbl3rd@ownlife ~ # vncviewer engsoc

TigerVNC Viewer for X version 1.0.90 - built Dec  8 2011 01:41:17
Copyright (C) 2002-2005 RealVNC Ltd.
Copyright (C) 2000-2006 TightVNC Group
Copyright (C) 2004-2009 Peter Astrand for Cendio AB
See http://www.tigervnc.org for information on TigerVNC.

Thu Dec 18 22:49:15 EST 2014
 CConn:       connected to host engsoc port 5900
 CConnection: Server supports RFB protocol version 3.8
 CConnection: Using RFB protocol version 3.8
 TXImage:     Using default colormap and visual, TrueColor, depth 24.
 CConn:       Using pixel format depth 24 (32bpp) little-endian rgb888
 CConn:       Using Tight encoding
...
^C
Thu Dec 18 22:49:15 EST 2014
 main:        CleanupSignalHandler called
tbl3rd@ownlife ~ 130#
```

At this point you have an RFB source on 'engsoc:5900' for the Twitter
Ticker demo overlay.
