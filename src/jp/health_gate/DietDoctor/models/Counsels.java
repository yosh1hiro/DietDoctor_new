package jp.health_gate.DietDoctor.models;

import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.core.KinveyClientCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * DDクリニックの相談を管理するクラス
 *
 * Created by kazhida on 2013/12/18.
 */
public class Counsels {

    public interface Callback {
        void onSuccess();
        void onFailure();
    }

    public static class Comment {
        private boolean fromUser;
        private Calendar time = Calendar.getInstance();
        private String comment;

        public boolean isFromUser() {
            return fromUser;
        }

        public String getComment() {
            return comment;
        }

        public Calendar getTime() {
            return time;
        }

        Comment() {}

        Comment(String comment) {
            fromUser = true;
            this.comment = comment;
        }
    }

    public static class Item {
        private String id;
        //  問診票の内容
        public String title;
        public String work;
        public String meal;
        public String exercise;
        public String snack;
        public String drink;
        public List<String> illness = new ArrayList<String>();
        //  定型回答
        private String byProfile;
        private String byCustom;
        private String byAction;
        private String byIllness;
        private List<String>  recommendedAction = new ArrayList<String>();
        //  相談のやりとり
        private List<Comment> comments = new ArrayList<Comment>();

        //  アクセさ
        public String getAnswerByProfile() {
            return byProfile;
        }

        public String getAnswerByCustom() {
            return byCustom;
        }

        public String getAnswerByAction() {
            return byAction;
        }

        public String getAnswerByIllness() {
            return byIllness;
        }

        public List<String> getRecommendedAction() {
            return recommendedAction;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public void addComment(String comment) {
            comments.add(new Comment(comment));
        }

        public boolean isAnswered() {
            if (comments.size() > 0) {
                int last = comments.size() - 1;
                return ! comments.get(last).isFromUser();
            } else {
                return false;
            }
        }
    }

    public static Item newCounsel(String comment) {
        Item item = new Item();
        item.addComment(comment);
        return item;
    }

    private static SimpleDateFormat format = new SimpleDateFormat(Backend.ISO8601UTC);

    public void send(final Item item, final Callback callback) {
        Backend.CounselRecord record = new Backend.CounselRecord();

        record.id = item.id;
        record.userId = ActiveUser.sharedInstance().getUserId();
        record.title = item.title;
        record.work = item.work;
        record.meal = item.meal;
        record.exercise = item.exercise;
        record.snack = item.snack;
        record.drink = item.drink;
        if (item.illness.size() > 0) {
            record.illness = new String[item.illness.size()];
            item.illness.toArray(record.illness);
        }
        record.byProfile = item.byProfile;
        record.byCustom = item.byCustom;
        record.byAction = item.byAction;
        record.byIllness = item.byIllness;
        if (item.recommendedAction.size() > 0) {
            record.recommendedAction = new String[item.recommendedAction.size()];
            item.recommendedAction.toArray(record.recommendedAction);
        }
        if (item.comments.size() > 0) {
            record.comments = new Backend.CommentRecord[item.comments.size()];
            for (int i = 0; i < record.comments.length; i++) {
                Comment comment = item.comments.get(i);
                record.comments[i] = new Backend.CommentRecord();
                if (comment.fromUser) {
                    record.comments[i].userId = ActiveUser.sharedInstance().getUserId();
                    record.comments[i].userName = ActiveUser.sharedInstance().getUserName();
                }
                record.comments[i].time = format.format(comment.time.getTime());
                record.comments[i].comment = comment.comment;
                if (i == 0) record.time = record.comments[i].time;
            }
        }
        Backend.sharedInstance().insertCounsel(record, new KinveyClientCallback<Backend.CounselRecord>() {
            @Override
            public void onSuccess(Backend.CounselRecord record) {
                ActiveUser.sharedInstance().decreaseTicket();
                callback.onSuccess();
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure();
            }
        });
    }

    private List<Item> items = new ArrayList<Item>();

    private Counsels() {
    }

    public List<Item> getItems() {
        return items;
    }

    private static Counsels shared;

    public static Counsels sharedInstance() {
        if (shared == null) shared = new Counsels();
        return shared;
    }

    public void load(final Callback callback) {
        items.clear();

        Backend.sharedInstance().loadCounsels(new KinveyListCallback<Backend.CounselRecord>() {
            @Override
            public void onSuccess(Backend.CounselRecord[] counselRecords) {
                if (counselRecords != null) {
                    for (Backend.CounselRecord record: counselRecords) {
                        Item item = new Item();
                        item.id = record.id;
                        item.title = record.title;
                        item.work = record.work;
                        item.meal = record.meal;
                        item.exercise = record.exercise;
                        item.snack = record.snack;
                        item.drink = record.drink;
                        if (record.illness != null) {
                            Collections.addAll(item.illness, record.illness);
                        }
                        item.byProfile = record.byProfile;
                        item.byCustom = record.byCustom;
                        item.byAction = record.byAction;
                        item.byIllness = record.byIllness;
                        if (record.recommendedAction != null) {
                            Collections.addAll(item.recommendedAction, record.recommendedAction);
                        }
                        if (record.comments != null) {
                            String userId = ActiveUser.sharedInstance().getUserId();
                            if (userId == null) userId = "";
                            for (Backend.CommentRecord commentRecord : record.comments) {
                                Comment comment = new Comment();
                                comment.fromUser = userId.equals(commentRecord.userId);
                                try {
                                    comment.time.setTime(format.parse(commentRecord.time));
                                } catch (ParseException e) {
                                    //nop
                                }
                                comment.comment = commentRecord.comment;
                                item.comments.add(comment);
                            }
                        }

                        if (item.recommendedAction.size() > 0) {
                            record.recommendedAction = new String[item.recommendedAction.size()];
                            item.recommendedAction.toArray(record.recommendedAction);
                        }
                        if (item.comments.size() > 0) {
                            record.comments = new Backend.CommentRecord[item.comments.size()];
                            for (int i = 0; i < record.comments.length; i++) {
                                Comment comment = item.comments.get(i);
                                record.comments[i] = new Backend.CommentRecord();
                                if (comment.fromUser) {
                                    record.comments[i].userId = ActiveUser.sharedInstance().getUserId();
                                    record.comments[i].userName = ActiveUser.sharedInstance().getUserName();
                                }
                                record.comments[i].time = format.format(comment.time.getTime());
                                record.comments[i].comment = comment.comment;
                            }
                        }
                        items.add(item);
                    }
                }
                callback.onSuccess();
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure();
            }
        });
    }
}
