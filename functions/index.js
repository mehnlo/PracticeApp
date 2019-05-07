'use strict';

const functions = require('firebase-functions');
const algoliasearch = require('algoliasearch');

// [START init_algolia]
// Initialize Algolia, requires installing Aloglia dependencies:
// https://www.algolia.com/doc/api-client/javascript/getting-started/#install
//
// App ID and API Key are stored in functions config variables
const ALGOLIA_APP_ID = functions.config().algolia.app_id;
const ALGOLIA_ADMIN_KEY = functions.config().algolia.api_key;
const ALGOLIA_SEARCH_KEY = functions.config().algolia.search_key;
const ALGOLIA_INDEX_NAME = 'usuarios';

const client = algoliasearch(ALGOLIA_APP_ID, ALGOLIA_ADMIN_KEY);
const index = client.initIndex(ALGOLIA_INDEX_NAME);
// [END init_algolia]

// [START update_index_function]
// Triggered when a document is written to for the first time.
exports.addFirestoreDataToAlgolia = functions.firestore
.document('users/{userId}')
.onCreate((snap, context) => {
    // Get the user documents
    const user = snap.data();
    // Add an 'objectID' field which Algolia requires
    user.objectID = context.params.userId;
    return index
      .saveObject(user)
      .then(() => {
        console.log(`User imported: ${user.objectID} into Algolia`);
      }).catch(error => {
        console.error('Error when importing user into Algolia', error);
        process.exit(1);
      });
});

// Triggered when a document already exists and has any value changed.
exports.updateFirestoreDataToAlgolia = functions.firestore
.document('users/{userId}')
.onUpdate((change, context) => {
  // Get the user documents
  const newUser = change.after.data();
  // Add an 'objectId' field which Algolia requires
  newUser.objectID = context.params.userId;
  return index
    .saveObject(newUser)
    .then(() => {
      console.log(`User updated: ${newUser.objectID} into Algolia`);
    }).catch(error => {
      console.error('Error when update user into Algolia', error);
      process.exit(1);
    });
});
// Triggered when a document with data is deleted.
exports.deleteFirestoreDataToAlgolia = functions.firestore
.document('users/{userId}')
.onDelete((snap, context) => {
  // Get Algolia's objectdID from the Firebase object Key
  const objectID = context.params.userId;
  // Remove the object from Algolia
  return index.
  deleteObject(objectID)
  .then(() => {
    console.log(`Firebase user: ${objectID} deleted from Algolia`);
  }).catch(error => {
    console.error('Error when deleting user from Algolia', error);
    process.exit(1);
  })
});
// [END update_index_function]

// [START get_firebase_user]
const admin = require('firebase-admin');
admin.initializeApp();

async function getFirebaseUser(req, res, next) {
  console.log('Check if request is authorized with Firebase ID token');

  if (!req.headers.authorization || !req.headers.authorization.startsWith('Bearer ')) {
    console.error(
      'No Firebase ID token was passed as a Bearer token in the Authorization header.',
      'Make sure you authorize your request by providing the following HTTP header:',
      'Authorization: Bearer <Firebase ID Token>'
      );
    return res.sendStatus(403);
  }

  let idToken;
  if (req.headers.authorization && req.headers.authorization.startsWith('Bearer ')) {
    console.log('Found \'Authorization\' header');
    idToken = req.headers.authorization.split('Bearer ')[1];
  }

  try {
    const decodedIdToken = admin.auth().verifyIdToken(idToken);
    console.log('ID Token correctly decoded', decodedIdToken);
    req.user = decodedIdToken;
    return next();
  } catch(error) {
    console.error('Error while verifying Firebase ID token:', error);
    return res.status(403).send('Unauthorized');
  }
}
// [END get_firebase_user]

// [START get_algolia_user_token]
// This complex HTTP function will be created as an ExpressJS app:
// https://expressjs.com/en/4x/api.html
const app = require('express')();

// We'll enable CORS support to allow the function to be invoked
// from our app client-side.
app.use(require('cors')({origin: true}));

// Then we'll also use a special 'getFirebaseUser' middleware which
// verifies the Authorization header and adds a `user` field to the
// incoming request:
// https://gist.github.com/abehaskins/832d6f8665454d0cd99ef08c229afb42
app.use(getFirebaseUser);

// Add a route handler to the app to generate the secured key
app.get('/', (req, res) => {
  // Create the params object as described in the Algolia documentation:
  // https://www.algolia.com/doc/guides/security/api-keys/#generating-api-keys
  const params = {
    // This filter ensures that only documents where author == user_id will be readable
    filters: `author:${req.user.user_id}`,
    // We also proxy the user_id as a unique token for this key.
    userToken: req.user.user_id,
  };

  // Call the Algolia API to generate a unique key based on our search key
  const key = client.generateSecuredApiKey(ALGOLIA_SEARCH_KEY, params);

  // Then return this key as {key: '...key'}
  res.json({key});
});

// Finally, pass our ExpressJS app to Cloud Functions as a function
// called 'getSearchKey';
exports.getSearchKey = functions.https.onRequest(app);
// [END get_algolia_user_token]
exports.loadFeed = functions.https.onCall((data, context) => {
  const email = context.auth.token.email || null;
  const reads = [];
  // Checking that the user is authenticated.
  if (!context.auth) {
    // Throwing an HttpsError so that the client gets the error details
    throw new functions.https.HttpsError('failed-precondition', 'The function must be called while authenticated.');
  }
  return new Promise((resolve, reject) => {
    const db = admin.firestore();
    const users = {};
    const following = [];
    const posts = [];
    // var exist = false;
    // Obtain all users that I am following
    db.collection(`following/${email}/userFollowing`)
    .get()
    .then(snapshot => {
      if (snapshot.empty) {
        console.log(`No matching documents in following/${email}/userFollowing.`);
        reject(`No matching documents in following/${email}/userFollowing.`);
      }

      // Obtain all my posts
      const promiseLoadMyPosts = loadPosts(db, posts, email);
      snapshot.forEach(doc => {
        let email = doc.id;
        let user = doc.data();
        let documentRef = db.doc(`posts/${doc.id}`);
        following.push(documentRef);
        // Obtain all posts of each user I am following
        let promise = loadPosts(db, posts, email);
        reads.push(promise);
      });

//      db.getAll(...following).then(docs => {
//         console.log(`${JSON.stringify(docs['userPosts'])}`);
//         console.log(`${JSON.stringify(docs)}`); // [{"_ref":{"_firestore":{"_settings...}]
//         console.log(`${docs[0].id}`); // agarciadk@alumnos.unex.es
//         console.log(`docs[0].data(): ${docs[0].data()}`); // [object Object]
//         console.log(`docs[0].data().get('title'): ${docs[0].data().get('title')}`); // error get is not a function
//         console.log(`docs[0].data().userPosts: ${docs[0].data().userPosts}`); // undefined
//         console.log(`docs[0].data().title: ${docs[0].data().title}`);
//         console.log(`docs[0].data().id: ${docs[0].data().id}`); // undefined
//         console.log(`${JSON.stringify(docs[0].data())}`); // {}
//         console.log(`First document ${JSON.stringify(docs[0])}`); // {"_ref":{"_firestore":{"_settings...}
//         console.log(`First document ${JSON.stringify(docs[0].userPosts)}`);
//         console.log(`Second document ${JSON.stringify(docs[1])}`);
//      });

      reads.push(promiseLoadMyPosts);
      Promise.all(reads).then(values => {
        values.forEach((value) => console.log(`value: ${value}`));
        // Sort posts using date
        // where the first item is the most recent posts
        // posts.sort((a, b) => {
        //   return new admin.firestore.Timestamp(b.date._seconds, b.date._nanoseconds) - new admin.firestore.Timestamp(a.date._seconds, a.date._nanoseconds);
        // });
        let postsStr = JSON.stringify(posts, null, '\t');
        console.log(`${postsStr}`);
        resolve(postsStr);
      });

    }).catch(reason => {
      console.error(`db.collection("users").get gets err, reason: ${reason}`);
      reject(reason);
    });
  });
});

function loadPosts(db, posts, email) {
  return new Promise((resolve) => {
    db.collection(`posts/${email}/userPosts`)
    .get()
    .then(snapshot => {
      if (snapshot.empty) {
        console.log(`No matching documents in posts/${email}/userPosts`);
        resolve();
      }
      snapshot.forEach(post => {
        let key = post.id;
        let postData = post.data();
        let item = {};
        item[key] = postData;
        // console.log(new admin.firestore.Timestamp(item[key].date._seconds, item[key].date._nanoseconds));
        posts.push(item);
      });
      resolve(posts);
    });
  });
}
