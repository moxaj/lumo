(ns lumo.classpath
  (:require [clojure.string :as string]
            [lumo.io :as io]))

(def fs (js/require "fs"))

(def path (js/require "path"))

(defn jar-file?
  "Returns true if file is a normal file with a .jar or .JAR extension."
  [f]
  (and (try
         (.. fs (statSync f) (isFile))
         (catch :default _ false))
       (or (.endsWith f ".jar")
           (.endsWith f ".JAR"))))

(defn filenames-in-jar
  "Returns a sequence of Strings naming the non-directory entries in
  the JAR file."
  [jar-file]
  (let [zip (.load (js/$$LUMO_GLOBALS.JSZip.) (fs.readFileSync jar-file))]
    (map #(. % -name)
      (filter (fn [f]
                (not (.-dir f)))
        (js/Object.values zip.files)))))

(defn files-in-jar
  "Returns a list of files in the JAR file."
  [jar-file]
  (->> jar-file
       (fs.readFileSync)
       (.load (js/$$LUMO_GLOBALS.JSZip.))
       (.-files)
       (js/Object.values)
       (filter #(not (.-dir %)))
       (map #(hash-map :name (.-name %)
                       :get-content-fn (fn [] (.asText %))
                       :url (path.join jar-file (.-name %))))))

(defn files-in-directory
  "Returns a list of files in a directory."
  [directory]
  (->> directory
       (fs.readdirSync)
       (map #(path.join directory %))
       (filter #(.. fs (statSync %) (isFile)))
       (map #(hash-map :name (path.basename %)
                       :get-content-fn (fn [] (io/slurp %))
                       :url %))))

(defn classpath
  "Returns a sequence of the elements on the classpath."
  []
  (seq (js/$$LUMO_GLOBALS.getSourcePaths)))

(defn- directory? [x]
  (try
    (.. fs (statSync x) (isDirectory))
    (catch :default _ false)))

(defn classpath-directories
  "Returns a sequence of the directories on the classpath."
  []
  (filter directory? (classpath)))

;; TODO: clojure.java.classpath returns a JarFile seq. should we return a
;; JSZip.files entry?
(defn classpath-jarfiles
  "Returns a sequence of the JAR files on the classpath."
  []
  (filter jar-file? (classpath)))

(defn add!
  "Add a directory, sequence of directories, JAR file or sequence of JAR to the Lumo classpath."
  [path-or-paths]
  (js/$$LUMO_GLOBALS.addSourcePaths (into-array (cond-> path-or-paths
                                                  (not (sequential? path-or-paths)) vector))))

(defn remove!
  "Remove a directory or JAR file from the Lumo classpath."
  [path]
  (js/$$LUMO_GLOBALS.removeSourcePath path))
