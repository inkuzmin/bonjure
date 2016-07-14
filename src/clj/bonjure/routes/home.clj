(ns bonjure.routes.home
  (:require [bonjure.layout :as layout]
            [bonjure.datastore :as datastore]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]))


;(defn set-user! [id {session :session}]
;  (-> (layout/render "home.html" {:id id})
;      (assoc :session (assoc session :id id))))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn generate-id []
  (uuid))

;(defonce uid (generate-id))

(defn generate-name []
  (str (rand-nth (vec #{"adaptable" "adventurous" "affable" "affectionate" "agreeable" "ambitious" "amiable" "amicable" "amusing" "brave" "bright" "broad-minded" "calm" "careful" "charming" "communicative" "compassionate"  "conscientious" "considerate" "convivial" "courageous" "courteous" "creative" "decisive" "determined" "diligent" "diplomatic" "discreet" "dynamic" "easygoing" "emotional" "energetic" "enthusiastic" "exuberant" "fair-minded" "faithful" "fearless" "forceful" "frank" "friendly" "funny" "generous" "gentle" "good" "gregarious" "hard-working" "helpful" "honest" "humorous" "imaginative" "impartial" "independent" "intellectual" "intelligent" "intuitive" "inventive" "kind" "loving" "loyal" "modest" "neat" "nice" "optimistic" "passionate" "patient" "persistent"  "pioneering" "philosophical" "placid" "plucky" "polite" "powerful" "practical" "pro-active" "quick-witted" "quiet" "rational" "reliable" "reserved" "resourceful" "romantic" "self-confident" "self-disciplined" "sensible" "sensitive" "shy" "sincere" "sociable" "straightforward" "sympathetic" "thoughtful" "tidy" "tough" "unassuming" "understanding" "versatile" "warmhearted" "willing" "witty"}))
       " "
       (rand-nth (vec #{"aardvark" "albatross" "alligator" "alpaca" "ant" "anteater" "antelope" "ape" "armadillo" "ass" "baboon" "badger" "barracuda" "bat" "bear" "beaver" "bee" "bison" "boar" "buffalo" "galago" "butterfly" "camel" "caribou" "cat" "caterpillar" "cattle" "chamois" "cheetah" "chicken" "chimpanzee" "chinchilla" "chough" "clam" "cobra" "cockroach" "cod" "cormorant" "coyote" "crab" "crane" "crocodile" "crow" "curlew" "deer" "dinosaur" "dog" "dogfish" "dolphin" "donkey" "dotterel" "dove" "dragonfly" "duck" "dugong" "dunlin" "eagle" "echidna" "eel" "eland" "elephant" "elephant seal" "elk" "emu" "falcon" "ferret" "finch" "fish" "flamingo" "fly" "fox" "frog" "gaur" "gazelle" "gerbil" "giant panda" "giraffe" "gnat" "gnu" "goat" "goose" "goldfinch" "goldfish" "gorilla" "goshawk" "grasshopper" "grouse" "guanaco" "guinea fowl" "guinea pig" "gull" "hamster" "hare" "hawk" "hedgehog" "heron" "herring" "hippopotamus" "hornet" "horse" "human" "hummingbird" "hyena" "jackal" "jaguar" "jay" "jay, blue" "jellyfish" "kangaroo" "koala" "komodo dragon" "kouprey" "kudu" "lapwing" "lark" "lemur" "leopard" "lion" "llama" "lobster" "locust" "loris" "louse" "lyrebird" "magpie" "mallard" "manatee" "marten" "meerkat" "mink" "mole" "monkey" "moose" "mouse" "mosquito" "mule" "narwhal" "newt" "nightingale" "octopus" "okapi" "opossum" "oryx" "ostrich" "otter" "owl" "ox" "oyster" "panther" "parrot" "partridge" "peafowl" "pelican" "penguin" "pheasant" "pig" "pigeon" "pony" "porcupine" "porpoise" "prairie dog" "quail" "quelea" "rabbit" "raccoon" "rail" "ram" "rat" "raven" "red deer" "red panda" "reindeer" "rhinoceros" "rook" "ruff" "salamander" "salmon" "sand dollar" "sandpiper" "sardine" "scorpion" "sea lion" "sea urchin" "seahorse" "seal" "shark" "sheep" "shrew" "shrimp" "skunk" "snail" "snake" "spider" "squid" "squirrel" "starling" "stingray" "stinkbug" "stork" "swallow" "swan" "tapir" "tarsier" "termite" "tiger" "toad" "trout" "turkey" "turtle" "vicuña" "viper" "vulture" "wallaby" "walrus" "wasp" "water buffalo" "weasel" "whale" "wolf" "wolverine" "wombat" "woodcock" "woodpecker" "worm" "wren" "yak" "zebra"}))))

(defn choose-color []
  (rand-nth (vec #{
                   "c1" "c2" "c3" "c4" "c5" "c6" "c7" "c8"
                   })))


(defn generate-user []
  {:username (generate-name)
   :color (choose-color)
   :ping (coerce/to-long (time/now))
   :status :online
   })

;(= (coerce/to-long (time/now))
;(time/in-millis (time/interval (time/date-time 1970 1 1) (time/now))))

(defn create-user-nx [id]
  (datastore/add-user-nx id (generate-user)))

(defn home-page [{sess :session}]
  (let [id (get sess :id (generate-id))
        ]
    (create-user-nx id)
    ;(log/info (datastore/get-users))

    (->
      (layout/render "home.html" {:uid id
                                  :username (:username (get (datastore/get-user id) id))})
      (assoc :session (assoc sess :id id)))))



;(home-page {})
;(set-user! (str (gensym)) req))


(defroutes home-routes
           (GET "/" req (home-page req))
           (GET "/docs" [] (response/ok (-> "docs/docs.md" io/resource slurp))))



