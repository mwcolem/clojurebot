(ns clojurebot.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
	    [clj-http.client :as client]
	    [cheshire.core :refer :all]
	    [clojurebot.commands.weather :as weather]
	    [clojure.string :as string]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def slack-bot-token "xoxb-5193775371-S0TAZnwgBh1mEAeCq5NA75pl")

(defn post-to-slack [message]
  (client/post "https://hooks.slack.com/services/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"
	{:body (generate-string {:channel "#bot-testing"
				:username "clojurebot"
				:icon_emoji ":ghost:"
				:text message})}))

(defmulti do-command (fn [command _ _] command))

(defmethod do-command :default [command user _]
  (post-to-slack (str user ": Unknown command " command ".")))

(defmethod do-command "weather" [_ user city]
  (if (= city "charleston")
    (post-to-slack (weather/get-current-conditions "Charleston" "SC"))))

(defn exec-user-command [{:keys [user text]}]
  (let [[_ command & search-terms] (string/split text #"\s")]
    (do-command command user search-terms)))

(defn process-incoming-webhook [username text]
  (exec-user-command {:user username :text text}))

(defroutes app-routes
  (GET "/" [] "Clojurebot")
  (POST "/" [user_name text] (process-incoming-webhook user_name text))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
