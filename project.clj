(defproject ops-game "1.0.0-SNAPSHOT"
  :description "Operational Game for CMBN"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [seesaw "1.0.6"]
                 [processing "1.5.1"]]
  :dev-dependencies [[marginalia "0.5.1"]
                     [swank-clojure "1.4.0-SNAPSHOT"]
                     [clojure-source "1.2.0"]]
  :jvm-opts ["-Djava.library.path=/Applications/Processing.app/Contents/Resources/Java/modes/java/libraries/opengl/library/macosx"
             "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"])

