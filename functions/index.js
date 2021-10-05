const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const DB_PROFILES = "profiles";
const DB_AUCTION = "auction";
const DB_TRANSACTION = "transaction";

exports.createUserProfile = functions.https.onCall(async(data, context) => {
    const details = {
        hold: 0,
        firstname: data.firstname,
        lastname: data.lastname,
        currentbalance: data.currentbalance,
        email: context.auth.token.email || null,
    };
    const refback = await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).set(details);
    return {
      result: `Success`,
      details: details,
      id: refback.id
    };
})

exports.addMoney = functions.https.onCall(async (data, context) => {
    const details = {
        currentbalance: admin.firestore.FieldValue.increment(data.currentbalance)
    }
    await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).update(details);
    return {
      result: `Success`,
      details: details,
    };
})

exports.postNewItem = functions.https.onCall(async (data, context) => {
    const result = await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).get();
    const profile = result.data();
    if (typeof profile === "undefined") {
      return {
        result: `Error: User not found`,
      };
    }
    if (profile.currentbalance >= 1) {
      const details = {currentbalance: admin.firestore.FieldValue.increment(-1)}
      await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).update(details);

      const writeResult = await admin.firestore().collection(DB_AUCTION).add(data);
      return {
        result: `Success`,
        details: details,
          wid: writeResult.id
      };
    }
    return {
      result: `Error: No funds!`,
    };
})

exports.cancelItem = functions.https.onCall(async (data, context) => {
  const result = await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).get();
  const profile = result.data();
  if (typeof profile === "undefined") {
    return {
      result: `Error: User not found`,
    };
  }

  const itemData = await admin.firestore().collection(DB_AUCTION).doc(data.itemId).get();
  if (typeof itemData.data() === "undefined") {
    return {
      result: `Error: Unknown item`,
    };
  }

  if (itemData.data().owner === context.auth.uid) {
    await admin.firestore().collection(DB_AUCTION).doc(data.itemId).delete();
    const bidWinner = itemData.winningBid.userId;
    const amount = itemData.winningBid.bidAmount;

    let userDetailsNew = {currentbalance: admin.firestore.FieldValue.increment(amount)}
    await admin.firestore().collection(DB_PROFILES).doc(bidWinner).update(userDetailsNew);
    return {
      result: `Success`,
    };
  }

  return {
    result: `User does not have permission to delete this item`,
  };
})

exports.bidOnItem = functions.https.onCall(async (data, context) => {
    const result = await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).get();
    const profile = result.data();
    if (typeof profile === "undefined") {
      return {
        result: `Error: User not found`,
      };
    }

    const itemData = await admin.firestore().collection(DB_AUCTION).doc(data.itemId).get();
    if (typeof itemData.data() === "undefined") {
      return {
        result: `Error: Unknown item`,
      };
    }

  const amount = data.amount + 1;
  if (profile.currentbalance >= amount) {
      const details = {currentbalance: admin.firestore.FieldValue.increment(-1)}
      await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).update(details);

      const itemDb = await admin.firestore().collection(DB_AUCTION).doc(data.itemId);
      await itemDb.update({previousbids: admin.firestore.FieldValue.arrayUnion(data),});

      const updWiningBid = await admin.firestore().collection(DB_AUCTION).doc(data.itemId).get();
      const bidArray = await updWiningBid.data().previousbids;

      const max = Math.max.apply(Math, bidArray.map(function (o) {
          return o.amount;
      })) // get win bid
      const winBid = bidArray.find(element => element.amount === max);

      await itemDb.update({
        winningBid: winBid.id,
      });

      return {
        result: `Success`,
        bidDetails: bidDetails,
      };
    } else {
        return {
          result: `Error: Insufficient funds!`,
        };
    }
})

exports.updateWinningBid = functions.firestore.document(DB_AUCTION + '/' + '{itemId}').onUpdate((change, context) => {

  const newBidWinner = change.after.data().winningBid.userId;
  const previousBidWinner = change.before.data().winningBid.userId;

  if (change.before.data().previousbids.length > change.after.data().previousbids.length) {
    let newmessage = {
      notification: {
        title: 'Auction Won',
        body: 'Your bid was the highest',
      },
      token: change.after.data().winningBid.noti_token,
    };
    admin.messaging().send(newmessage)
        .then((response) => {
          console.log('Success: ', response);
        })
        .catch((error) => {
          console.log('Error: ', error);
        });

    if (change.before.data().winningBid.userId !== change.before.data().owner) {
      let amount1 = change.before.data().winningBid.bidAmount;
      let prev_details = {
          currentbalance: admin.firestore.FieldValue.increment(amount1),
        hold: admin.firestore.FieldValue.increment(-amount1),
      }
      const docRef = admin.firestore().collection(DB_PROFILES).doc(previousBidWinner);
      const _ = docRef.update(prev_details);
    }

    if (change.after.data().winningBid.userId !== change.after.data().owner) {
      const docRefNew = admin.firestore().collection(DB_PROFILES).doc(newBidWinner);
      let amount2 = change.after.data().winningBid.bidAmount;
      let after_details = {
          currentbalance: admin.firestore.FieldValue.increment(-amount2),
        hold: admin.firestore.FieldValue.increment(amount2),
      }
      const _ = docRefNew.update(after_details);
    }

  } else {
    const newmessage1 = {
      notification: {
        title: 'Auction Lost',
        body: 'Your bid isnt the highest',
      },
      token: change.before.data().winningBid.noti_token,
    };
    admin.messaging().send(newmessage1)
        .then((response) => {
          console.log('Success: ', response);
        })
        .catch((error) => {
          console.log('Error: ', error);
        });

    if (change.after.data().winningBid.userId !== change.after.data().owner) {
      let amount3 = change.after.data().winningBid.bidAmount;
      let new_details = {
          currentbalance: admin.firestore.FieldValue.increment(-amount3),
        hold: admin.firestore.FieldValue.increment(amount3),
      }
      const docRef = admin.firestore().collection(DB_PROFILES).doc(newBidWinner);
      const balanceWriteResult = docRef.update(new_details);
    }

    if (change.before.data().winningBid.userId !== change.before.data().owner) {
      const docRefNew = admin.firestore().collection(DB_PROFILES).doc(previousBidWinner);
      let amount4 = change.before.data().winningBid.bidAmount;
      let prev_details = {
          currentbalance: admin.firestore.FieldValue.increment(amount4),
        hold: admin.firestore.FieldValue.increment(-amount4),
      }
      const balanceOnHoldWriteResult = docRefNew.update(prev_details);
    }
  }
});

exports.updateOnItemDelete = functions.firestore.document(DB_AUCTION + '/' + '{itemId}').onDelete((change, context) => {
  const bidWinner = change.data().winningBid.userId;
  const amount = change.data().winningBid.bidAmount;

  let new_details = { hold: admin.firestore.FieldValue.increment(-amount) }
  const docRef = admin.firestore().collection(DB_PROFILES).doc(bidWinner);
  const balanceWriteResult = docRef.update(new_details);
});

exports.cancelBid = functions.https.onCall(async (data, context) => {
    const result = await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).get();
    const profile = result.data();
    if (typeof profile === "undefined") {
      return {
        result: `Error: User not found`,
      };
    }

    const itemData = await admin.firestore().collection(DB_AUCTION).doc(data.itemId).get();
    if (typeof itemData.data() === "undefined") {
      return {
        result: `Error: Unknown item`,
      };
    }

    if (itemData.data().winningBid.userId === context.auth.uid) {
        let filteredItems = itemData.data().previousbids.filter(item => item.bidder_id !== itemData.data().winningBid.bidder_id)
        let winBid;
        for (let i = 0; i < filteredItems.length; i++) {
          let max = Math.max.apply(Math, filteredItems.map(function (o) { return o.amount; }))
          winBid = filteredItems.find(element => element.amount === max);
          const checkUserBalance = await admin.firestore().collection(DB_PROFILES).doc(winBid.bidder_id).get();
          if (checkUserBalance.data().currentbalance < winBid.amount) {
            filteredItems = filteredItems.filter(item => item.bidder_id !== winBid.bidder_id)
          } else {
            break;
          }
        }
        const itemDoc = await admin.firestore().collection(DB_AUCTION).doc(data.itemId);
        await itemDoc.update({winningBid: winBid, previousbids: filteredItems,});
        return {
          result: `Success`,
        };
    } else {
      return {
        result: `Error: You do not have the winning bid.`,
      };
    }
})

exports.acceptBidOnItem = functions.https.onCall(async (data, context) => {
    const result = await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).get();
    const profile = result.data();
    if (typeof profile === "undefined") {
      return {
        result: `Error: User not found`,
      };
    }

    const itemData = await admin.firestore().collection(DB_AUCTION).doc(data.itemId).get();
    if (typeof itemData.data() === "undefined") {
      return {
        result: `Error: Unknown item`,
      };
    }

    if (itemData.data().owner === context.auth.uid && itemData.data().winningBid.amount >= itemData.data().finalBid) {

      const winnerId = itemData.data().winningBid.bidder_id;
      const itemName = itemData.data().item.name;

      const amount = itemData.data().winningBid.amount;

      const details = {
          currentbalance: admin.firestore.FieldValue.increment(amount),
      }

      await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).update(details);

      await admin.firestore().collection(DB_AUCTION).doc(data.itemId).delete();

      let date_ob = new Date();
      const transDetails = {
        history: admin.firestore.FieldValue.arrayUnion({
          seller_id: context.auth.uid,
          item: itemName,
          price: amount,
          date: date_ob,
        }),
      }

      await admin.firestore().collection(DB_TRANSACTION).doc(winnerId).update(transDetails);

      return {
        result: `Success`,
      };
    }
    return {
      result: `Error: Bid amount should be more than Final Bid Amount.`,
    };
})

exports.getHistory = functions.https.onCall(async (data, context) => {
    const result = await admin.firestore().collection(DB_TRANSACTION).doc(context.auth.uid).get();
    const history = result.data();
    if (typeof history === "undefined") {
      return {
        result: `Error: Unknown user`,
      };
    }
    return {
      result: {
        "history": history.history,
      },
    };
})