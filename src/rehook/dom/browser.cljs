(ns rehook.dom.browser
  (:require
   ["react" :as react]
   [rehook.util :as util]))

(defn handle-type
  [e ctx $]
  (cond
    (keyword? e)
    (name e)

    (util/rehook-component? e)
    (e ctx $)

    (sequential? e)
    (apply react/Fragment e)

    :else e))

(defn bootstrap
  ([ctx ctx-f props-f e]
   (let [ctx (ctx-f ctx e)]
     (react/createElement (handle-type e ctx (partial bootstrap ctx ctx-f props-f)))))
  ([ctx ctx-f props-f e args]
   (let [ctx (ctx-f ctx e)]
     (react/createElement (handle-type e ctx (partial bootstrap ctx ctx-f props-f)) (props-f args))))
  ([ctx ctx-f props-f e args & children]
   (let [ctx (ctx-f ctx e)]
     (apply react/createElement (handle-type e ctx (partial bootstrap ctx ctx-f props-f)) (props-f args) children))))