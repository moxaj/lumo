(ns lumo.util
  (:require-macros lumo.util)
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [cljs.js :as cljs]
            [cljs.compiler :as comp]
            [cljs.reader :as edn]
            [lumo.io :as io]))

;; next line is auto-generated by the build-script - Do not edit!
;(def ^:dynamic *clojurescript-version*)

(defn ^String clojurescript-version
  "Returns clojurescript version as a printable string."
  []
  cljs.core/*clojurescript-version*
  #_(if-not (nil? *clojurescript-version*)
    (str
     (:major *clojurescript-version*)
     "."
     (:minor *clojurescript-version*)
     (when-let [i (:incremental *clojurescript-version*)]
       (str "." i))
     (when-let [q (:qualifier *clojurescript-version*)]
       (str "." q))
     (when (:interim *clojurescript-version*)
       "-SNAPSHOT"))
    "0.0.0000"))

(defn compiled-by-version [f]
  #_(with-open [reader (io/reader f)]
    (let [match (->> reader line-seq first
                     (re-matches #".*ClojureScript (\d+\.\d+\.\d+).*$"))]
      (or (and match (second match)) "0.0.0000"))))

(defn distinct-by
  ([f coll]
   (let [step (fn step [xs seen]
                (lazy-seq
                  ((fn [[x :as xs] seen]
                     (when-let [s (seq xs)]
                       (let [v (f x)]
                         (if (contains? seen v)
                           (recur (rest s) seen)
                           (cons x (step (rest s) (conj seen v)))))))
                    xs seen)))]
     (step coll #{}))))

(defn output-directory
  ([opts] (output-directory opts "out"))
  ([opts default]
   {:pre [(or (nil? opts) (map? opts))]}
   (or (:output-dir opts) default)))

(defn debug-prn
  [& args]
  (binding [*print-fn* *print-err-fn*]
    (apply println args)))

(defn directory? [path]
  (try
    (.isDirectory (js/$$LUMO_GLOBALS.fs.lstatSync path))
    (catch :default _
      false)))

(defn mkdirs [p]
  (let [target-dir (-> p js/$$LUMO_GLOBALS.path.resolve (js/$$LUMO_GLOBALS.path.resolve ".."))]
    (reduce (fn [acc d]
              (let [new-path (js/$$LUMO_GLOBALS.path.join acc d)]
                (cond-> new-path
                  (not (js/$$LUMO_GLOBALS.fs.existsSync new-path))
                  js/$$LUMO_GLOBALS.fs.mkdirSync)
                new-path))
      "/" (rest (string/split target-dir #"/")))))

(defn bundled-resource? [x]
  (and (goog/isObject x) (= (.-type x) "bundled")))

(defn jar-resource? [x]
  (and (goog/isObject x) (= (.-type x) "jar")))

(defn resource? [x]
  (and (goog/isObject x) (= (.-type x) "file")))

(defn last-modified [path]
  (cond
    (bundled-resource? path) (.getTime (js/Date.))
    (jar-resource? path) (.getTime (.-date path))
    (resource? path) (.getTime (.-mtime (js/$$LUMO_GLOBALS.fs.statSync (.-src path))))
    :else (.getTime (.-mtime (js/$$LUMO_GLOBALS.fs.statSync path)))))

(defn changed? [a b]
  (not (== (last-modified a) (last-modified b))))

(defn munge-path [ss]
  (comp/munge (str ss)))

(defn ns->relpath
  "Given a namespace as a symbol return the relative path. May optionally
  provide the file extension, defaults to :cljs."
  ([ns] (ns->relpath ns :cljs))
  ([ns ext]
   (str (string/replace (munge-path ns) \. \/) "." (name ext))))

(defn ns->source
  "Given a namespace as a symbol return the corresponding resource if it exists."
  [ns]
  (or (io/resource (ns->relpath ns :cljs))
      (io/resource (ns->relpath ns :cljc))))

(defn path [x]
  (cond
    (string? x) (js/$$LUMO_GLOBALS.path.resolve x)
    (or (resource? x) (bundled-resource? x)) (.-src x)
    (jar-resource? x) (str "file:" (.-jarPath x) "!/" (.-src x))))

(defn ext
  "Given a file, url or string return the file extension."
  [x]
  (let [file (cond
               (string? x) x

               (or (resource? x)
                   (bundled-resource? x)
                   (jar-resource? x))
               (.-src x))]
    (last (string/split file #"\."))))

(defn path-seq
  [file-str]
  ;; TODO: need to quote path-sep?
  (->> (.-sep js/$$LUMO_GLOBALS.path)
       re-pattern
       (string/split file-str)))

(defn to-path
  ([parts]
     (to-path parts (.-sep js/$$LUMO_GLOBALS.path)))
  ([parts sep]
    (apply str (interpose sep parts))))

(defn get-name
  "Given a file or url return the last component of the path."
  [x]
  (last (string/split (path x) #"/")))

(defn relative-name
  "Given a file return a path relative to the working directory. Given a
   URL return the JAR relative path of the resource."
  [x]
  (letfn [(strip-user-dir [s]
            (string/replace s
              (str (js/process.cwd) (.-sep js/$$LUMO_GLOBALS.path)) ""))]
    ;; TODO: distinguish between JAR / normal file
    #_(if (file? x)
      (strip-user-dir (.getAbsolutePath x))
      (let [f (.getFile x)]
        (if (string/includes? f ".jar!/")
          (last (string/split f #"\.jar!/"))
          (strip-user-dir f))))))

(defn content-sha [s]
  (let [digest (js/$$LUMO_GLOBALS.crypto.createHash "sha1")]
    (.update digest s)
    (.toUpperCase (.digest digest "hex"))))

(defn line-seq [path]
  (string/split (io/slurp path) #"\n"))

(defn build-options [f]
  (let [reader f]
    (let [match (->> reader line-seq first
                  (re-matches #".*ClojureScript \d+\.\d+\.\d+ (.*)$"))]
      (and match (edn/read-string (second match))))))

(defn map-merge [a b]
  (if (and (map? a) (map? b))
    (loop [ks (seq (keys a)) ret a b' b]
      (if ks
        (let [k (first ks)]
          (if (contains? b' k)
            (recur
              (next ks)
              (assoc ret k (map-merge (get ret k) (get b' k)))
              (dissoc b' k))
            (recur (next ks) ret b')))
        (merge ret b')))
    a))

(defn file-seq [dir]
  (tree-seq
    (fn [f] (.isDirectory (js/$$LUMO_GLOBALS.fs.statSync f) ()))
    (fn [d] (map #(js/$$LUMO_GLOBALS.path.join d %) (js/$$LUMO_GLOBALS.fs.readdirSync d)))
    dir))

(defn to-target-file
  ([target-dir ns-info]
   (to-target-file target-dir ns-info "js"))
  ([target-dir {:keys [ns source-file] :as ns-info} ext]
   (let [src-ext (if source-file
                   (lumo.util/ext source-file)
                   "cljs")
         ns      (if (or (= src-ext "clj")
                       (and (= ns 'cljs.core) (= src-ext "cljc")))
                   (symbol (str ns "$macros"))
                   ns)
         relpath (string/split (munge-path (str ns)) #"\.")
         parents (cond-> (butlast relpath)
                   target-dir (conj target-dir))]
     (cond->> (js/$$LUMO_GLOBALS.path.join (str (last relpath) (str "." ext)))
       (seq parents)
       (to-path parents)))))

(defn get-absolute-path [file-or-resource]
  (cond
    (string? file-or-resource) (js/$$LUMO_GLOBALS.path.resolve file-or-resource)

    (resource? file-or-resource)
    (.-src file-or-resource)

    (or (jar-resource? file-or-resource)
        (bundled-resource? file-or-resource))
    file-or-resource

    :else (do
            (js/console.log file-or-resource (.-constructor file-or-resource) (object? file-or-resource)
              (= (.-type file-or-resource) "jar"))
            (throw (ex-info "should never happen!" {:x file-or-resource})))))

(defn set-last-modified [file time]
  (js/$$LUMO_GLOBALS.fs.utimesSync file time time))

(defn file-seq [dir]
  (tree-seq
    (fn [f] (.isDirectory (js/$$LUMO_GLOBALS.fs.statSync f) ()))
    (fn [d] (map #(js/$$LUMO_GLOBALS.path.join d %) (js/$$LUMO_GLOBALS.fs.readdirSync d)))
    dir))

(defn levenshtein-distance
  "The the minimum number of single-element edits needed to
  transform s in to t."
  [s t]
  (let [f (fn [f s t]
            (cond
              (empty? s) (count t)
              (empty? t) (count s)
              :else (let [cost (if (= (first s) (first t))
                                 0
                                 1)]
                      (min (inc (f f (rest s) t))
                        (inc (f f s (rest t)))
                        (+ cost (f f (rest s) (rest t)))))))
        g (memoize f)]
    (g g s t)))

(defn suggestion
  "Provides a best suggestion for an unknown, taken from knowns,
  minimizing the Levenshtein distance, returning nil if threshold
  cannot be satisfied."
  [threshold unknown knowns]
  (let [distance     (partial levenshtein-distance unknown)
        closest      (apply min-key distance knowns)
        closest-dist (distance closest)]
    (when (<= closest-dist threshold)
      closest)))

(defn unknown-opts
  "Takes a set of passed opt keys and known opt keys and for each
  unknown opt key returns a vector of the key and its (potentially
  nil) suggestion."
  [passed knowns]
  {:pre [(set? passed) (set? knowns)]}
  (for [unknown (set/difference passed knowns)]
    [unknown (some-> (suggestion 3 (str unknown) (map str knowns))
               (subs 1)
               keyword)]))
