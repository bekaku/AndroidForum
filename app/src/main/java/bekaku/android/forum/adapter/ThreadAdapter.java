package bekaku.android.forum.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import bekaku.android.forum.R;
import bekaku.android.forum.model.ForumSetting;
import bekaku.android.forum.model.ThreadModel;
import bekaku.android.forum.util.ApiUtil;
import bekaku.android.forum.util.GlideUtil;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder> implements View.OnClickListener {

    private Context c;
    private List<ThreadModel> list;
    private ForumSetting forumSetting;

    public ThreadAdapter(Context context, List<ThreadModel> listParam, ForumSetting setting) {
        this.list = listParam;
        this.c = context;
        this.forumSetting = setting;
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<ThreadModel> listParam) {
        list.addAll(listParam);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThreadAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_list_thread, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ThreadAdapter.ViewHolder viewHolder, int iposition) {

        ThreadModel threadModel = list.get(iposition);

        Glide.with(c)
                .load(threadModel.getUserAccountPicture())
                .apply(GlideUtil.circleOption())
                .into(viewHolder.userImg);

        viewHolder.username.setText(threadModel.getUserAccountName());
        viewHolder.datetime.setText(threadModel.getCreated());
        viewHolder.subject.setText(threadModel.getSubject());

        viewHolder.voteUp.setText(String.valueOf(threadModel.getVoteUp()));
        viewHolder.voteDown.setText(String.valueOf(threadModel.getVoteDown()));
        viewHolder.comments.setText(String.valueOf(threadModel.getPostCount()));

        if(threadModel.isUserVoteUp()){
            viewHolder.voteUpIcon.setTextColor(c.getResources().getColor(R.color.color_green_1));
        }else{
            viewHolder.voteUpIcon.setTextColor(c.getResources().getColor(R.color.text_mute));
        }
        if(threadModel.isUserVoteDown()){
            viewHolder.voteDownIcon.setTextColor(c.getResources().getColor(R.color.color_red_1));
        }else{
            viewHolder.voteDownIcon.setTextColor(c.getResources().getColor(R.color.text_mute));
        }

        if(threadModel.isUserComment()){
            viewHolder.commentsIcon.setTextColor(c.getResources().getColor(R.color.color_yellow_1));
        }else{
            viewHolder.commentsIcon.setTextColor(c.getResources().getColor(R.color.text_mute));
        }

        viewHolder.voteUpIcon.setOnClickListener(this);
        viewHolder.voteDownIcon.setOnClickListener(this);
        viewHolder.commentsIcon.setOnClickListener(this);
        //set tag for on click icon
        viewHolder.voteUpIcon.setTag(iposition);
        viewHolder.voteDownIcon.setTag(iposition);
        viewHolder.commentsIcon.setTag(iposition);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onClick(View view) {

        TextView tag;
        int posiotion;
        ThreadModel model;
        switch (view.getId()){
            case R.id.vote_up_icon :
                tag = (TextView) view;
                posiotion = (Integer) tag.getTag();
                model = list.get(posiotion);
                new VoteThreadUp().execute(model);
                break;
            case R.id.vote_down_icon :
                tag = (TextView) view;
                posiotion = (Integer) tag.getTag();
                model = list.get(posiotion);
                new VoteThreadDown().execute(model);
                break;
            case R.id.comments_icon :
                tag = (TextView) view;
                posiotion = (Integer) tag.getTag();
                model = list.get(posiotion);
                commentAction(model);
                break;

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class VoteThreadUp extends AsyncTask<ThreadModel, Void, ThreadModel>{

        @Override
        protected ThreadModel doInBackground(ThreadModel... models) {
            ThreadModel model = models[0];

            HashMap<String, String> params = new HashMap<>();
            params.put("_thread_id", String.valueOf(model.getThreadId()));
            params.put("_user_account_id", String.valueOf(forumSetting.getUserId()));

            String response = ApiUtil.okHttpGet(forumSetting.getApiMainUrl() + "/vote/upthread.php", params);
            try {

                JSONObject json = new JSONObject(response);
                JSONObject data = json.getJSONObject("data");
                model.setVoteUp(data.getInt("votes_up"));
                model.setVoteDown(data.getInt("votes_down"));
                model.setPostCount(data.getInt("post_count"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return model;
        }

        @Override
        protected void onPostExecute(ThreadModel threadModel) {
            super.onPostExecute(threadModel);

            threadModel.setUserVoteUp(!threadModel.isUserVoteUp());
            notifyDataSetChanged();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class VoteThreadDown extends AsyncTask<ThreadModel, Void, ThreadModel>{

        @Override
        protected ThreadModel doInBackground(ThreadModel... models) {
            ThreadModel model = models[0];

            HashMap<String, String> params = new HashMap<>();
            params.put("_thread_id", String.valueOf(model.getThreadId()));
            params.put("_user_account_id", String.valueOf(forumSetting.getUserId()));

            String response = ApiUtil.okHttpGet(forumSetting.getApiMainUrl() + "/vote/downthread.php", params);

            try {

                JSONObject json = new JSONObject(response);
                JSONObject data = json.getJSONObject("data");
                model.setVoteUp(data.getInt("votes_up"));
                model.setVoteDown(data.getInt("votes_down"));
                model.setPostCount(data.getInt("post_count"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return model;
        }

        @Override
        protected void onPostExecute(ThreadModel threadModel) {
            super.onPostExecute(threadModel);

            threadModel.setUserVoteDown(!threadModel.isUserVoteDown());
            notifyDataSetChanged();
        }
    }



    private void commentAction(ThreadModel threadModel){

        //open comment activity
        System.out.println("comments_icon==>"+threadModel.getSubject());
    }




    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView userImg;
        public TextView username;
        public TextView datetime;
        public TextView subject;

        public TextView voteUpIcon;
        public TextView voteUp;
        public TextView voteDownIcon;
        public TextView voteDown;
        public TextView commentsIcon;
        public TextView comments;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImg = itemView.findViewById(R.id.user_img);
            username = itemView.findViewById(R.id.user_name);
            datetime = itemView.findViewById(R.id.datetime);
            subject = itemView.findViewById(R.id.thread_subject);

            voteUpIcon = itemView.findViewById(R.id.vote_up_icon);
            voteUp = itemView.findViewById(R.id.vote_up);
            voteDownIcon = itemView.findViewById(R.id.vote_down_icon);
            voteDown = itemView.findViewById(R.id.vote_down);
            commentsIcon = itemView.findViewById(R.id.comments_icon);
            comments = itemView.findViewById(R.id.comments_count);
        }
    }
}
