'use strict';

const functions = require('firebase-functions');
const algoliasearch = require('algoliasearch');
const REGION = 'europe-west1';

// [START init_algolia]
// Initialize Algolia, requires installing Aloglia dependencies:
// https://www.algolia.com/doc/api-client/javascript/getting-started/#install
//
// App ID and API Key are stored in functions config variables
const ALGOLIA_APP_ID = functions.config().algolia.app_id;
const ALGOLIA_ADMIN_KEY = functions.config().algolia.api_key;
const ALGOLIA_SEARCH_KEY = functions.config().algolia.search_key;
const ALGOLIA_INDEX_NAME = 'users';

const client = algoliasearch(ALGOLIA_APP_ID, ALGOLIA_ADMIN_KEY);
const index = client.initIndex(ALGOLIA_INDEX_NAME);
// [END init_algolia]

const PICTURE_FEMALE = functions.config().picture.female;
const PICTURE_MALE = functions.config().picture.male;
const PICTURE_UNSPECIFIED = functions.config().picture.unspecified;

// [START update_index_function]
// Triggered when a document is written to for the first time.
exports.addFirestoreDataToAlgolia = functions.region(REGION).firestore
    .document('users/{userId}')
    .onCreate((snap, context) => {
        // Get the user documents
        const user = snap.data();
        // Doesn't have photoUrl
        if (user.photoUrl === undefined || user.photoUrl === null) return snap.ref.update({photoUrl: PICTURE_UNSPECIFIED});
        // Add an 'objectID' field which Algolia requires
        user.objectID = context.params.userId;
        return index
            .saveObject(user)
            // eslint-disable-next-line promise/always-return
            .then(() => {
                console.log(`User imported: ${user.objectID} into Algolia`);
            }).catch(error => {
                console.error('Error when importing user into Algolia', error);
                process.exit(1);
            });
    });

// Triggered when a document already exists and has any value changed.
exports.updateFirestoreDataToAlgolia = functions.region(REGION).firestore
    .document('users/{userId}')
    .onUpdate((change, context) => {
        // Get the user documents
        const newUser = change.after.data();
        const previousUser = change.before.data();
        // Only if the update involve gender
        if(previousUser.gender !== newUser.gender) {
            if (newUser.gender === 'unspecified') return change.after.ref.update({photoUrl: PICTURE_UNSPECIFIED});
            else if (newUser.gender === 'male') return change.after.ref.update({photoUrl: PICTURE_MALE});
            else if (newUser.gender === 'female') return change.after.ref.update({photoUrl: PICTURE_FEMALE});
        }
        // Add an 'objectId' field which Algolia requires
        newUser.objectID = context.params.userId;
        return index
            .saveObject(newUser)
            // eslint-disable-next-line promise/always-return
            .then(() => {
                console.log(`User updated: ${newUser.objectID} into Algolia`);
            }).catch(error => {
                console.error('Error when update user into Algolia', error);
                process.exit(1);
            });
    });
// Triggered when a document with data is deleted.
exports.deleteFirestoreDataToAlgolia = functions.region(REGION).firestore
    .document('users/{userId}')
    .onDelete((snap, context) => {
      // Get Algolia's objectdID from the Firebase object Key
      const objectID = context.params.userId;
      // Remove the object from Algolia
      return index.
      deleteObject(objectID)
      // eslint-disable-next-line promise/always-return
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
exports.getSearchKey = functions.region(REGION).https.onRequest(app);
// [END get_algolia_user_token]
exports.loadFeed = functions.region(REGION).https.onCall((data, context) => {
    const email = context.auth.token.email || null;
    const db = admin.firestore();
    let promises = [];
    let userFollowing = [email];
    let result;
    // Checking that the user is authenticated.
    if (!context.auth) {
        // Throwing an HttpsError so that the client gets the error details
        throw new functions.https.HttpsError('failed-precondition', 'The function must be called while authenticated.');
    }

    /**
     * Initialize userFollowing array with the user I'm following
     * @returns {Promise<any>}
     */
    function getFollowing() {
        return new Promise(resolve => {
            let collectionPath = `following/${email}/userFollowing`;
            let collectionRef = db.collection(collectionPath);
            // eslint-disable-next-line promise/catch-or-return
            collectionRef.get().then(querySnapshot => {
               // eslint-disable-next-line promise/always-return
               if (querySnapshot.empty) {
                   let msg = `No matching documents in ${collectionPath}.`;
                   console.log(msg);
                   resolve();
               }
               resolve(querySnapshot.docs.forEach(docs => userFollowing.push(docs.id)));
            })
        });
    }

    /**
     * Initialize promises array with the promises of gets posts of every user in userFollowing array
     * @returns {Promise<void>}
     */
    async function getPromises() {
        await getFollowing();
        userFollowing.forEach(emailUserFollowing => {
            promises.push(new Promise(resolve => {
                let collectionPath =  `posts/${emailUserFollowing}/userPosts`;
                let collectionRef = db.collection(collectionPath);
                // eslint-disable-next-line promise/catch-or-return
                collectionRef.get().then(querySnapshot => {
                    // eslint-disable-next-line promise/always-return
                    if (querySnapshot.empty) {
                        console.log(`No matching documents in ${collectionPath}.`);
                        resolve();
                    } else {
                        let documentPath = `users/${emailUserFollowing}`;
                        let documentRef = db.doc(documentPath);
                        // eslint-disable-next-line promise/catch-or-return,promise/always-return,promise/no-nesting
                        documentRef.get().then(documentSnapshot => {
                            resolve(querySnapshot.docs.map(doc => Object.assign(doc.data(), {id: doc.id}, {author: documentSnapshot.get('email'), profilePic: documentSnapshot.get('photoUrl')})));
                        });
                    }

                })
            }));
        });
    }

    /**
     * Get the posts of every user within users that the authorized user is following
     * @returns {Promise<{feed: *}>}
     */
    async function getResult() {
        await getPromises();
        // eslint-disable-next-line promise/always-return,promise/catch-or-return
        await Promise.all(promises).then(values => {
            // FIXED: (#11 Cloud functions:loadFeed)
            // eslint-disable-next-line promise/always-return
            if (values !== undefined) {
                let arrayFeed = [];
                // values typeof Array(Array(Object)) values[userFollowing...[post...{}]]
                values.forEach((userFollowing) => {
                    if (userFollowing !== undefined) {
                        userFollowing.forEach((post) => {
                            arrayFeed.push(post);
                        });
                    }
                });
                result = JSON.stringify(arrayFeed, null, '\t');
                // result typeof Array(Object) result[post...{}]
            }
        });
        return {feed: result};
    }
    return getResult();

});

/**
 * When a user is delete from auth this function triggers, then remove all info associated with that user
 * in database and storage
 * Uses cases:
 *      (1) When there is nothing to delete in storage or firestore,
 *          the location of bucket users/${email} doesn't exist or
 *          the documentReference doesn't exist.
 *      (2) When there is something to delete in storage or firestore.
 * @type {CloudFunction<UserRecord>}
 */
exports.removeUser = functions.region(REGION).auth.user()
    .onDelete(user => {
        // Get the email of the deleted user.
        // Test(passed): expected email john@doe.com
        const email = user.email;
        const db = admin.firestore();
        const storage = admin.storage();
        const bucket = storage.bucket();
        // Test(passed): declaration
        const batch = db.batch();
        const userRef = db.doc(`users/${email}`);
        const postsRef = db.doc(`posts/${email}`);
        const countersRef = db.doc(`counters/${email}`);
        const followingRef = db.doc(`following/${email}`);
        const followersRef = db.doc(`followers/${email}`);

        batch.delete(userRef);
        batch.delete(postsRef);
        batch.delete(countersRef);
        batch.delete(followingRef);
        batch.delete(followersRef);

            // Test(passed): delete the location path users/${email}
            // Test: delete the location path posts/${email}
            // Test(passed): delete the location path counters/${email}
            // Test(passed): delete the location path following/${email}
            // Test(passed): delete the location path followers/${email}
            // Test(passed): remove all files in bucketRef
        return Promise.all(
            batch.commit(),
            bucket.deleteFiles({ prefix: `users/${email}` })
        );
    });
