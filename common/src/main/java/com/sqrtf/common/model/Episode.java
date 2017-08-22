package com.sqrtf.common.model;

/**
 * Created by roya on 2017/5/27.
 */

public class Episode {
    public Integer status;
    public Integer episode_no;
    public Long update_time;
    public String name;
    public Integer bgm_eps_id;
    public String bangumi_id;
    public String airdate;
    public String name_cn;
    public String thumbnail;
    public Long delete_mark;
    public Long create_time;
    public String duration;
    public String id;

    public WatchProgress watch_progress;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Episode episode = (Episode) o;

        if (bangumi_id != null ? !bangumi_id.equals(episode.bangumi_id) : episode.bangumi_id != null)
            return false;
        return id != null ? id.equals(episode.id) : episode.id == null;
    }

    @Override
    public int hashCode() {
        int result = bangumi_id != null ? bangumi_id.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
