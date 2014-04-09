(ns leiningen.init-jsvc
  (:require [clojure.java.io :as io]))

(defn resource-input-stream [res-name]
  (.getResourceAsStream (.getContextClassLoader (Thread/currentThread)) res-name))

(def init-d-template (slurp (resource-input-stream "init-d-template")))
(def makefile-template (slurp (resource-input-stream "makefile-template")))

(defn gen-init-d-script [project]
  (let [{:keys [name version description main]} project]
    [name
     (format init-d-template
       name
       version
       description
       main)]))

(defn gen-makefile [project]
  (let [{:keys [name version description]} project]
    ["Makefile"
     (format makefile-template
       name
       version)]))

(defn create-output-dir [path]
  (.mkdirs (java.io.File. path)))

(defn create-script [dir name content]
  (let [path (str dir "/" name)]
    (spit path content)
    (doto (java.io.File. path)
      (.setExecutable true))))

(defn init-jsvc
  "A leiningen plugin that generates a Makefile for make install/uninstall and a
   etc/init.d script for easy starting of a jsvc based daemon. Intented to be used together with lein-tar."
  [project]
  (let [artifact-dir "pkg"
        etc-init-d-dir (str artifact-dir "/etc/init.d")]
    (create-output-dir artifact-dir)
    (create-output-dir etc-init-d-dir)
    (apply create-script artifact-dir (gen-makefile project))
    (apply create-script etc-init-d-dir (gen-init-d-script project))
    (println (str "*** Done generating init-d script and Makefile, see the " artifact-dir " directory"))))
