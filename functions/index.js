const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const DB_PROFILES = "profiles";
const DB_AUCTION = "auction";
const DB_TRANSACTION = "transaction";
const DB_HISTORY = "history";

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

      data['created_at'] = new Date()
      const writeResult = await admin.firestore().collection(DB_AUCTION).add(data);
      return {
        result: `Success`,
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

    await admin.firestore().collection(DB_AUCTION).doc(data.itemId).delete();
    const bidWinner = itemData.data().bids[parseInt(itemData.data().winningBid) - 1].bidder_id;
    const amount = itemData.data().bids[parseInt(itemData.data().winningBid) - 1].amount;

    await admin.firestore().collection(DB_PROFILES).doc(bidWinner).update({currentbalance: admin.firestore.FieldValue.increment(amount)});
    return {
      result: `Success`,
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
      await itemDb.update({bids: admin.firestore.FieldValue.arrayUnion(data),});

      const updWiningBid = await admin.firestore().collection(DB_AUCTION).doc(data.itemId).get();
      const bidArray = await updWiningBid.data().bids;

      const max = Math.max.apply(Math, bidArray.map(function (o) {
          return o.amount;
      })) // get win bid
      const winBid = bidArray.find(element => element.amount === max);

      await itemDb.update({
        winningBid: winBid.id,
      });

      return {
        result: `Success`,
      };
    } else {
        return {
          result: `Error: Insufficient funds!`,
        };
    }
})

exports.updateWinningBid = functions.firestore.document(DB_AUCTION + '/' + '{itemId}').onUpdate((change, context) => {

    const afterdata = change.after.data()
    const beforedata = change.before.data()

    let newBidWinner = null
    if (typeof afterdata.winningBid !== "undefined") {
        newBidWinner = afterdata.bids[parseInt(afterdata.winningBid) - 1];
    }
    let previousBidWinner = null
    if (typeof beforedata.winningBid !== "undefined") {
        previousBidWinner = beforedata.bids[parseInt(beforedata.winningBid) - 1];
    }

  if (beforedata.bids.length > afterdata.bids.length) {
      if(newBidWinner !== null) {
          const payload = {
              data: {},
              notification:{
                  title: 'Auction Update',
                  body: 'Your bid is the highest',
              },
              token: newBidWinner.noti_token,
          };
          admin.messaging().send(payload)
              .then((response) => {
                  console.log('Success: ', response);
                  return {success: true};
              })
              .catch((error) => {
                  console.log('Error: ', error);
                  return {error: error.code};
              });
      }

    if (previousBidWinner !== null && previousBidWinner.bidder_id !== beforedata.owner) {
      let amount1 = previousBidWinner.amount;
      let prev_details = {
        currentbalance: admin.firestore.FieldValue.increment(amount1),
        hold: admin.firestore.FieldValue.increment(-amount1),
      }
      const docRef = admin.firestore().collection(DB_PROFILES).doc(previousBidWinner.bidder_id);
      const _ = docRef.update(prev_details);
    }

    if (newBidWinner !== null && newBidWinner.bidder_id !== afterdata.owner) {
      const docRefNew = admin.firestore().collection(DB_PROFILES).doc(newBidWinner.bidder_id);
      let amount2 = newBidWinner.amount;
      let after_details = {
        currentbalance: admin.firestore.FieldValue.increment(-amount2),
        hold: admin.firestore.FieldValue.increment(amount2),
      }
      const _ = docRefNew.update(after_details);
    }

  } else {
      if(previousBidWinner !== null){
          const payload = {
              data: {},
              notification: {
                  title: 'Auction Update',
                  body: 'Your bid isnt the highest anymore',
              },
              token: previousBidWinner.noti_token,
          };
          admin.messaging().send(payload)
              .then((response) => {
                  console.log('Success: ', response);
                  return {success: true};
              })
              .catch((error) => {
                  console.log('Error: ', error);
                  return {error: error.code};
              });
      }
      if(newBidWinner !== null){
          const payload = {
              data: {
              },
              notification: {
                  title: 'Auction Update',
                  body: 'Your bid is the highest',
              },
              token: newBidWinner.noti_token,
          };
          admin.messaging().send(payload)
              .then((response) => {
                  console.log('Success: ', response);
                  return {success: true};
              })
              .catch((error) => {
                  console.log('Error: ', error);
                  return {error: error.code};
              });
      }

    if (newBidWinner !== null && newBidWinner.bidder_id !== afterdata.owner) {
      let amount3 = newBidWinner.amount;
      let new_details = {
        currentbalance: admin.firestore.FieldValue.increment(-amount3),
        hold: admin.firestore.FieldValue.increment(amount3),
      }
      const docRef = admin.firestore().collection(DB_PROFILES).doc(newBidWinner.bidder_id);
      const WriteResult = docRef.update(new_details);
    }

    if (previousBidWinner !== null && previousBidWinner.bidder_id !== beforedata.owner) {
      const docRefNew = admin.firestore().collection(DB_PROFILES).doc(previousBidWinner.bidder_id);
      let amount4 = previousBidWinner.amount;
      let prev_details = {
        currentbalance: admin.firestore.FieldValue.increment(amount4),
        hold: admin.firestore.FieldValue.increment(-amount4),
      }
      const OnHoldWriteResult = docRefNew.update(prev_details);
    }
  }
    return {
        result: `Success`,
    };
});

exports.updateOnItemDelete = functions.firestore.document(DB_AUCTION + '/' + '{itemId}').onDelete((change, context) => {
    const changedata = change.data()
    const winninBid = changedata.bids[parseInt(changedata.winningBid) - 1]
  const bidWinner = winninBid.bidder_id;
  const amount = winninBid.amount;

  let new_details = { hold: admin.firestore.FieldValue.increment(-amount) }
  const docRef = admin.firestore().collection(DB_PROFILES).doc(bidWinner);
  return docRef.update(new_details);
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

    if (itemData.data().bids[parseInt(itemData.data().winningBid) - 1].bidder_id === context.auth.uid) {
        let filteredItems = itemData.data().bids.filter(item => item.bidder_id !== itemData.data().bids[parseInt(itemData.data().winningBid) - 1].bidder_id)
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
        await itemDoc.update({winningBid: winBid.id, bids: filteredItems});
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
      const winnerId = itemData.data().bids[parseInt(itemData.data().winningBid) - 1].bidder_id;
      const itemName = itemData.data().name;

      const amount = itemData.data().bids[parseInt(itemData.data().winningBid) - 1].amount;

      await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).update({currentbalance: admin.firestore.FieldValue.increment(amount)});

      await admin.firestore().collection(DB_AUCTION).doc(data.itemId).delete();

      let ddate = new Date();
      const transDetails = {
          seller_id: context.auth.uid, seller_name: data.owner_name,
          item: itemName,
          price: amount,
          date: ddate,
      }

      await admin.firestore().collection(DB_HISTORY).doc(winnerId).collection(DB_TRANSACTION).add(transDetails);

      return {
        result: `Success`,
      };
})