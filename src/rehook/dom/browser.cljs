(ns rehook.dom.browser
  (:require
   ["react" :as react]
   [rehook.util :as util]))

(defn handle-type
  [e ctx $]
  (cond
    (or (keyword? e) (string? e))
    (name e)

    (util/rehook-component? e)
    (e ctx $)

    (sequential? e)
    (apply react/Fragment e)

    :else
    e))

(defn bootstrap
  ([ctx ctx-f e]
   (let [ctx (ctx-f ctx e)]
     (react/createElement (handle-type e ctx (partial bootstrap ctx ctx-f)))))
  ([ctx ctx-f e args]
   (let [ctx (ctx-f ctx e)]
     (react/createElement (handle-type e ctx (partial bootstrap ctx ctx-f)) (clj->js args))))
  ([ctx ctx-f e args & children]
   (let [ctx (ctx-f ctx e)]
     (apply react/createElement (handle-type e ctx (partial bootstrap ctx ctx-f)) (clj->js args) children))))