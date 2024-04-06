[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![codecov](https://codecov.io/gh/UnitVectorY-Labs/crossfiresync/graph/badge.svg?token=66F2VVP2J1)](https://codecov.io/gh/UnitVectorY-Labs/crossfiresync)

# crossfiresync

Real-time synchronization between GCP Firestore instances across regions using PubSub.

## Synchronization Mechanism

To replicate the data in a Firestore collection between different regions a Cloud Function, the `FirestoreChangePublisher`, is triggered by `google.cloud.firestore.document.v1.written` so it receives all inserts, updates, and events for the documents.  These changes are written to a Pub/Sub topic.  Another Cloud Function, the `PubSubChangeConsumer` is triggered by the Pub/Sub topic.  These Cloud Functions are configured in each region that the it is desired to have the data replicate between.

The aspiration of this application is to allow for full read and write access to the Firestore collections in each region having them replicated and consistent in all of the regions where replication is enabled.  However, the limitations of how Firestore and Pub/Sub work make it so that it is impossible to guarentee consistency between all of the region.

In order to efficiently accomplish the replication some additional attributes must be added to the document.  These fields help control the replication of data between the regions.  An application using the documents **must not** write these attributes as that can adversely impact data consistency.  An application using the documents **should not** delete these attributes, but doing so will not impact replication as that is treated as a modification to the document resulting in the data being replicated.

- `crossfiresync:timestamp` 
- `crossfiresync:sourcedatabase`
- `crossfiresync:delete`
