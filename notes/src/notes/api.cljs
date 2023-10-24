(ns notes.api
  (:require
    [clojure.string :as string]
    [central :as central]))

(defn json [obj] (js/JSON.stringify (clj->js obj)))
(defn parse-json [json] (-> json js/JSON.parse (js->clj :keywordize-keys true)))

(defn token [] (central/Users.token))

(defn request [url options]
  (->
    (central/Api.Blob.request url (clj->js options))
    (.then (fn [res] (if (= (.-status res) 200) res (js/reject res))))
    (.then (fn [res] (.json res)))
    (.then #(js->clj % :keywordize-keys true))))

(defn central-request [url options]
  (->
    (central/Api.Central.request url (clj->js options))
    (.then (fn [res] (.json res)))
    (.then #(js->clj % :keywordize-keys true))))

(defn get-user []
  (->
    (central-request "/users/validate" {:method "POST" :body (json {:token (token)})})
    (.then #(:user %))))

(defn note-build [note]
  (let [title (:title note)
        folder (string/split title #"/" 2)]
    (assoc (assoc note
                  :short-title
                  (if (= (count folder) 2) (second folder) title))
            :folder
            (if (= (count folder) 2) (first folder) "default"))))

(defn note-apply [note]
  (let [last-modified (first (filter #(= (:key %) "last-modified") (:tags note)))
        title (string/replace (:key note) #"^notes/" "")]
    (note-build
      {:title title
       :body (->> note :body parse-json)
       :last-modified (if (not (nil? last-modified)) (:value last-modified))})))

(defn list-notes [email]
  (->
    (request "/blobs/list" {:method "POST" :body (json {:bucket email :prefix "notes/"})})
    (.then #(:blobs %))
    (.then #(map note-apply %))))

(defn get-note [email title]
  (->
    (request "/blobs/get" {:method "POST" :body (json {:bucket email :key (str "notes/" title)})})
    (.then #(note-apply %))))

(defn create-note [email title body]
  (request "/blobs/create" {:method "POST" :body (json {:bucket email
                                                        :key (str "notes/" title)
                                                        :body (json body)
                                                        :tags [{:key "last-modified" :value (str (-> js/Date .now))}]})}))
(defn delete-note [email title]
  (request "/blobs/delete" {:method "POST" :body (json {:bucket email :key (str "notes/" title)})}))
