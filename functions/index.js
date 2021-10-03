const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const DB_PROFILES = "profiles";
const DB_AUCTION = "auction";
const DB_TRANSACTION = "transaction";

exports.createUser = functions.https.onCall(async (data, context) => {
    const details = {
      currentbalance: parseFloat(data.balance),
      hold: 0,
      firstname: data.firstname,
      lastname: data.lastname,
      email: context.auth.token.email || null,
    }
    const refback = await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).set(details);
    return {
      result: `Success`,
      details: details,
      id: refback.id
    };
})

exports.fetchUser = functions.https.onCall(async (data, context) => {
  const result = await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).get();
  const profile = result.data();
  if (typeof profile === "undefined") {
    return {
      result: `Error: User not found`,
    };
  }
  return {
    result: {
      "firstname": profile.firstname,
      "lastname": profile.lastname,
      "email": profile.email ,
      "currentbalance": profile.currentbalance,
      "hold": profile.hold,
    },
  };
})

exports.addBalance = functions.https.onCall(async (data, context) => {
    const details = {
      balance: admin.firestore.FieldValue.increment(parseFloat(data.balance))
    }
    await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).update(details);
    return {
      result: `Success`,
      details: details,
    };
})

exports.postItem = functions.https.onCall(async (data, context) => {
  // data has item obj
    const result = await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).get();
    const profile = result.data();
    if (typeof profile === "undefined") {
      return {
        result: `Error: User not found`,
      };
    }
    if (profile.currentbalance >= 1) {
      const details = {balance: admin.firestore.FieldValue.increment(-1)}
      await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).update(details);
      objj = JSON.parse(data.item);

      const item = {
        owner_id: context.auth.uid,
        item_obj: objj,
        minFinalBid: parseFloat(data.minFinalBid),
        winningBid: {
          bidAmount: parseFloat(data.startBid),
          noti_token: profile.noti_token,
          userId: context.auth.uid,
        },
        previousbids: [
          {
            bidAmount: parseFloat(data.startBid),
            noti_token: profile.noti_token,
            userId: context.auth.uid,
          },
        ],
      }
      const refback = await admin.firestore().collection(DB_AUCTION).doc(item.id).set(details);
      return {
        result: `Success`,
        details: details,
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
    const amount = parseFloat(itemData.winningBid.bidAmount);

    let userDetailsNew = {balance: admin.firestore.FieldValue.increment(amount)}
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

  const amount = parseFloat(data.bidAmount) + 1;
  if (profile.balance >= amount && (parseFloat(data.bidAmount) >= parseFloat(itemData.data().winningBid.bidAmount))) {
      const details = {balance: admin.firestore.FieldValue.increment(-1)}
      await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).update(details);

      const bidDetails = {
        bidAmount: parseFloat(data.bidAmount),
        userId: context.auth.uid,
        noti_token: profile.noti_token,
      }

      const itemDb = await admin.firestore().collection(DB_AUCTION).doc(data.itemId);
      await itemDb.update({
        previousbids: admin.firestore.FieldValue.arrayUnion(bidDetails),
      });

      const updWiningBid = await admin.firestore().collection(DB_AUCTION).doc(data.itemId).get();
      const bidArray = await updWiningBid.data().previousbids;

      const max = Math.max.apply(Math, bidArray.map(function (o) { return o.bidAmount; }))

      const winBid = bidArray.find(element => element.bidAmount === max);
      await itemDb.update({
        winningBid: winBid,
      });

      return {
        result: `Success`,
        bidDetails: bidDetails,
      };
    } else {
      if (amount < parseFloat(itemData.data().winningBid.bidAmount)) {
        return {
          result: `Error: Bid must be greater than or equal to the currentBid`,
        };
      }
      else {
        return {
          result: `Error: Insufficient funds!`,
        };
      }
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
      let amount1 = parseFloat(change.before.data().winningBid.bidAmount);
      let prev_details = {
        balance: admin.firestore.FieldValue.increment(amount1),
        balanceonhold: admin.firestore.FieldValue.increment(-amount1),
      }
      const docRef = admin.firestore().collection(DB_PROFILES).doc(previousBidWinner);
      const _ = docRef.update(prev_details);
    }

    if (change.after.data().winningBid.userId !== change.after.data().owner) {
      const docRefNew = admin.firestore().collection(DB_PROFILES).doc(newBidWinner);
      let amount2 = parseFloat(change.after.data().winningBid.bidAmount);
      let after_details = {
        balance: admin.firestore.FieldValue.increment(-amount2),
        balanceonhold: admin.firestore.FieldValue.increment(amount2),
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
      let amount3 = parseFloat(change.after.data().winningBid.bidAmount);
      let new_details = {
        balance: admin.firestore.FieldValue.increment(-amount3),
        balanceonhold: admin.firestore.FieldValue.increment(amount3),
      }
      const docRef = admin.firestore().collection(DB_PROFILES).doc(newBidWinner);
      const balanceWriteResult = docRef.update(new_details);
    }

    if (change.before.data().winningBid.userId !== change.before.data().owner) {
      const docRefNew = admin.firestore().collection(DB_PROFILES).doc(previousBidWinner);
      let amount4 = parseFloat(change.before.data().winningBid.bidAmount);
      let prev_details = {
        balance: admin.firestore.FieldValue.increment(amount4),
        balanceonhold: admin.firestore.FieldValue.increment(-amount4),
      }
      const balanceOnHoldWriteResult = docRefNew.update(prev_details);
    }
  }
});

exports.updateOnItemDelete = functions.firestore.document(DB_AUCTION + '/' + '{itemId}').onDelete((change, context) => {
  const bidWinner = change.data().winningBid.userId;
  const amount = parseFloat(change.data().winningBid.bidAmount);

  let new_details = { balanceonhold: admin.firestore.FieldValue.increment(-amount) }
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
      if(itemData.data().owner === context.auth.uid){
        await admin.firestore().collection(DB_AUCTION).doc(data.itemId).delete();
        return {
          result: `Success`,
        };
      }
      else{
        let filteredItems = itemData.data().previousbids.filter(item => item.userId !== itemData.data().winningBid.userId)
        let winBid;
        for (let i = 0; i < filteredItems.length; i++) {
          let max = Math.max.apply(Math, filteredItems.map(function (o) { return o.bidAmount; }))
          winBid = filteredItems.find(element => element.bidAmount === max);
          const checkUserBalance = await admin.firestore().collection(DB_PROFILES).doc(winBid.userId).get();
          if (checkUserBalance.data().balance < winBid.bidAmount) {
            filteredItems = filteredItems.filter(item => item.userId !== winBid.userId)
          } else {
            break;
          }
        }
        const itemDoc = await admin.firestore().collection(DB_AUCTION).doc(data.itemId);
        await itemDoc.update({winningBid: winBid, previousbids: filteredItems,});
        return {
          result: `Success`,
        };
      }
    } else if(itemData.data().owner === context.auth.uid){
      await admin.firestore().collection('Items').doc(data.itemId).delete();
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

    if (itemData.data().owner === context.auth.uid && itemData.data().winningBid.bidAmount >= itemData.data().minfinalbid) {

      const winnerId = itemData.data().winningBid.userId;
      const itemName = itemData.data().item.name;

      const amount = parseFloat(itemData.data().winningBid.bidAmount);

      const details = {
        balance: admin.firestore.FieldValue.increment(amount),
      }

      await admin.firestore().collection(DB_PROFILES).doc(context.auth.uid).update(details);

      const itemDeleted = await admin.firestore().collection(DB_AUCTION).doc(data.itemId).delete();

      let date_ob = new Date();
      const transDetails = {
        history: admin.firestore.FieldValue.arrayUnion({
          sellerId: context.auth.uid,
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