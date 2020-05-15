package com.escapp.controller;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import com.escapp.model.Contest;
import com.escapp.model.ContestHeader;
import com.escapp.model.EscObjectList;
import com.escapp.model.Rate;
import com.escapp.model.UserContest;
import com.escapp.view.App;

import static com.escapp.controller.ContestManager.getContestManager;

/**
 * Created by laura on 8.12.14.
 */
public class ContestTask {

    private static ContestTask contestTask = null;
    private static ContestTask getInstance() {
        if (contestTask == null) {
            contestTask = new ContestTask();
        }
        return contestTask;
    }
    private ArrayList<Worker> itemQueue = new ArrayList<Worker>();

    private static abstract class Item {

        public enum Type {
            INITIALIZE,
            UPDATE_CONTESTS_FOR_TYPE,
            SET_CONTEST,
            SET_RATE,
            SET_RATE_MODE,
            UPDATE_CONTEST,
            RESET_RATES,
            DELETE_COMMENTS
        };

        private Type type = null;

        Item(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }

    }

    private class InitializeItem extends Item {
        public InitializeItem() {
            super(Type.INITIALIZE);
        }
    }

    private class UpdateContestsForTypeItem extends Item {
        private int contestType = 0;

        public UpdateContestsForTypeItem(int contestType) {
            super(Type.UPDATE_CONTESTS_FOR_TYPE);
            this.contestType = contestType;
        }

        public int getContestType() {
            return contestType;
        }
    }

    private class SetContestItem extends Item {
        private String contestId = null;
        private boolean headerChanged = false;
        public SetContestItem(String contestId) {
            super(Type.SET_CONTEST);
            this.contestId = contestId;
        }
        public String getContestId() {
            return contestId;
        }
        public boolean isHeaderChanged() {
            return headerChanged;
        }
        public void setHeaderChanged(boolean headerChanged) {
            this.headerChanged = headerChanged;
        }
    }

    private class SetRateItem extends Item {
        private Rate rate = null;
        public SetRateItem(Rate rate) {
            super(Type.SET_RATE);
            this.rate = rate;
        }
        public Rate getRate() {
            return rate;
        }

    }

    private class SetRateModeItem extends Item {
        private UserContest.RateMode rateMode = null;
        private String contestId = null;
        public SetRateModeItem(String contestId, UserContest.RateMode rateMode) {
            super(Type.SET_RATE_MODE);
            this.rateMode = rateMode;
            this.contestId = contestId;
        }
        public UserContest.RateMode getRateMode() {
            return rateMode;
        }
        public String getContestId() {
            return contestId;
        }
    }

    private class ResetRatesItem extends Item {
        private UserContest.RateMode rateMode = null;
        private String contestId = null;
        public ResetRatesItem(String contestId, UserContest.RateMode rateMode) {
            super(Type.RESET_RATES);
            this.rateMode = rateMode;
            this.contestId = contestId;
        }
        public UserContest.RateMode getRateMode() {
            return rateMode;
        }
        public String getContestId() {
            return contestId;
        }
    }

    private class DeleteCommentsItem extends Item {
        private String contestId = null;
        public DeleteCommentsItem(String contestId) {
            super(Type.DELETE_COMMENTS);
            this.contestId = contestId;
        }
        public String getContestId() {
            return contestId;
        }
    }

    private class UpdateContestItem extends Item {
        private ContestHeader contestHeader = null;

        private boolean updated = false;

        public UpdateContestItem(ContestHeader contestHeader) {
            super(Type.UPDATE_CONTEST);
            this.contestHeader = contestHeader;
        }
        public ContestHeader getContestHeader() {
            return contestHeader;
        }

        public boolean isUpdated() {
            return updated;
        }

        public void setUpdated(boolean updated) {
            this.updated = updated;
        }

    }


    public static void initializeContests() {
        getInstance().doInitializeContests();
    }

    public void doInitializeContests() {
        itemQueue.add(new Worker(new InitializeItem()));
        runNextTask();
    }

    public static void updateContestsForType(int contestType) {
        getInstance().doUpdateContestsForType(contestType);
    }

    public void doUpdateContestsForType(int contestType) {
        itemQueue.add(new Worker(new UpdateContestsForTypeItem(contestType)));
        runNextTask();
    }

    public static void setContest(String contestId) {
        getInstance().doSetContest(contestId);
    }

    public void doSetContest(String contestId) {
        itemQueue.add(new Worker(new SetContestItem(contestId)));
        runNextTask();
    }

    public static void setRate(Rate rate) {
        getInstance().doSetRate(rate);
    }

    public void doSetRate(Rate rate) {
        itemQueue.add(new Worker(new SetRateItem(rate)));
        runNextTask();
    }

    public static void setRateMode(String contestId, UserContest.RateMode rateMode) {
        getInstance().doSetRateMode(contestId, rateMode);
    }

    public void doSetRateMode(String contestId, UserContest.RateMode rateMode) {
        itemQueue.add(new Worker(new SetRateModeItem(contestId, rateMode)));
        runNextTask();
    }

    public static void resetRateMode(String contestId, UserContest.RateMode rateMode) {
        getInstance().doResetRateMode(contestId, rateMode);
    }

    public void doResetRateMode(String contestId, UserContest.RateMode rateMode) {
        itemQueue.add(new Worker(new ResetRatesItem(contestId, rateMode)));
        runNextTask();
    }

    public static void deleteComments(String contestId) {
        getInstance().doDeleteComments(contestId);
    }

    public void doDeleteComments(String contestId) {
        itemQueue.add(new Worker(new DeleteCommentsItem(contestId)));
        runNextTask();
    }

    public static void updateContest(Contest contest) {
        // TODO: copy id instead of header?
        ContestHeader contestHeader = contest.getHeader();
        if (!contest.getUserData().getUpdateFromXml() && contest.getUserData().getUpdateFromWeb()) {
            if (contestHeader.getUpdateSrc().length() > 0) {
                getInstance().doUpdateContest(contestHeader);
            } else {
                contest.getUserData().setUpdateFromWeb(false);
            }
        }
    }

    public void doUpdateContest(ContestHeader contestHeader) {
        itemQueue.add(new Worker(new UpdateContestItem(contestHeader)));
        runNextTask();
    }

    private void runNextTask() {
        runNextTask(null);
    }

    private void runNextTask(Worker itemToRemove) {
        if (itemToRemove != null) {
            itemQueue.remove(itemToRemove);
        }
        if (itemQueue.size() > 0) {
            Worker head = itemQueue.get(0);
            if (head != null && head.getStatus() == AsyncTask.Status.PENDING) {
                head.execute(head.getItem());
            }
        }
    }

    private class Worker extends AsyncTask<Item, Void, Item> {
        private Item item = null;

        public Worker(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }

        protected Item doInBackground(Item... params) {
            Item item = params[0];
            if (item.getType() == Item.Type.INITIALIZE) {
                ContestManager.getContestManager().findAvailableContests();
            } else if (item.getType() == Item.Type.SET_CONTEST) {
                SetContestItem setContestItem = (SetContestItem) item;
                if (setContestItem.getContestId() != null) {
                    try {
                        boolean headerChanged =
                                ContestManager.getContestManager().setCurrentContest(setContestItem.getContestId());
                        setContestItem.setHeaderChanged(headerChanged);
                    } catch (Exception e) {
                        Logger.e("Exception when setting active contest: " + e.toString());
                    }
                }
            } else if (item.getType() == Item.Type.UPDATE_CONTESTS_FOR_TYPE) {
                ContestManager.getContestManager().findAvailableContests();
            } else if (item.getType() == Item.Type.SET_RATE) {
                SetRateItem setRateItem = (SetRateItem)item;
                ContestManager.getContestManager().setRate(setRateItem.getRate());

            } else if (item.getType() == Item.Type.SET_RATE_MODE) {
                SetRateModeItem setRateModeItem = (SetRateModeItem)item;
                ContestManager.getContestManager().setRateMode(
                        setRateModeItem.getContestId(), setRateModeItem.getRateMode());
            } else if (item.getType() == Item.Type.RESET_RATES) {
                ResetRatesItem resetRatesItem = (ResetRatesItem)item;
                ContestManager.getContestManager().resetRateMode(
                        resetRatesItem.getContestId(), resetRatesItem.getRateMode());
            } else if (item.getType() == Item.Type.DELETE_COMMENTS) {
                DeleteCommentsItem deleteCommentsItem = (DeleteCommentsItem)item;
                ContestManager.getContestManager().deleteComments(
                        deleteCommentsItem.getContestId());
            } else if (item.getType() == Item.Type.UPDATE_CONTEST) {
                UpdateContestItem updateContestItem = (UpdateContestItem)item;
                if (WebUpdater.getUpdatedContestData(updateContestItem.getContestHeader())) {
                    updateContestItem.setUpdated(true);
                }
            }
            return item;
        }

        protected void onPostExecute(Item result) {
            Intent intent = null;
            if (result.getType() == Item.Type.INITIALIZE) {
                sendUpdatedContestHeaderIntent();
            } else if (result.getType() == Item.Type.UPDATE_CONTESTS_FOR_TYPE) {
                UpdateContestsForTypeItem updateItem = (UpdateContestsForTypeItem)item;
                sendUpdatedContestHeaderIntent(updateItem.getContestType());
            } else if (result.getType() == Item.Type.SET_CONTEST) {
                SetContestItem setContestItem = (SetContestItem)item;
                Contest contest =
                        getContestManager().findCachedContest(setContestItem.getContestId());
                intent = new Intent(Contest.EXTRA_NAME);
                intent.putExtra(Contest.EXTRA_NAME, contest);
                LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
                // Notify of contest header change when contest is updated
                if (setContestItem.isHeaderChanged()) {
                    sendUpdatedContestHeaderIntent();
                }
                // Check once per app launch if we have updated data on server
                updateContest(contest);
            } else if (result.getType() == Item.Type.SET_RATE_MODE ||
                    result.getType() == Item.Type.RESET_RATES ||
                    result.getType() == Item.Type.SET_RATE) {
                intent = new Intent(Rate.EXTRA_NAME);
                LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
            } else if (result.getType() == Item.Type.UPDATE_CONTEST) {
                UpdateContestItem updateContestItem = (UpdateContestItem)item;
                if (updateContestItem.isUpdated()) {
                    Contest contest =
                            getContestManager().findCachedContest(updateContestItem.getContestHeader().getId());
                    ContestHeader updatedHeader =
                            getContestManager().getHeaderForId(((UpdateContestItem) item).getContestHeader().getId());
                    contest.getUserData().setUpdateFromWeb(false);
                    intent = new Intent(ContestHeader.EXTRA_NAME);
                    intent.putExtra(ContestHeader.EXTRA_NAME, updatedHeader);
                    intent.putExtra("updatedDataFound", true);
                    LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
                }
            }
            runNextTask(this);
        }

        private void sendUpdatedContestHeaderIntent() {
            List<EscObjectList<ContestHeader>> allContests = getContestManager().getAvailableContests();
            Logger.d("sendUpdatedContestHeaderIntent contest types count: " + allContests.size());

            Intent intent = new Intent(ContestHeader.EXTRA_NAME_LIST);
            for (int i = 0; i < ContestManager.CONTEST_TYPES.length; i++) {
                intent.putExtra(ContestManager.CONTEST_TYPES[i], allContests.get(i));
            }
            LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
        }

        private void sendUpdatedContestHeaderIntent(int contestType) {
            List<EscObjectList<ContestHeader>> allContests = getContestManager().getAvailableContests();

            if (contestType >= 0 && contestType < allContests.size()) {
                Logger.d("sendUpdatedContestHeaderIntent for contest type: " + contestType);

                Intent intent = new Intent(ContestHeader.EXTRA_NAME_LIST);
                intent.putExtra(ContestManager.CONTEST_TYPES[contestType], allContests.get(contestType));
                LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
            }

        }
    }
}
