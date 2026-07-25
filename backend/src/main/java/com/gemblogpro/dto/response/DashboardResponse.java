package com.gemblogpro.dto.response;

import java.util.List;

/**
 * Response body for {@code GET /api/admin/dashboard}.
 * Replaces {@code res.json({success:true, dashboardData:{blogs, comments, drafts, recentBlogs}})}
 * in {@code adminController.js}'s {@code getDashBoard}.
 */
public class DashboardResponse {

    private boolean success;
    private DashboardData dashboardData;

    public DashboardResponse() {
    }

    public DashboardResponse(boolean success, DashboardData dashboardData) {
        this.success = success;
        this.dashboardData = dashboardData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DashboardData getDashboardData() {
        return dashboardData;
    }

    public void setDashboardData(DashboardData dashboardData) {
        this.dashboardData = dashboardData;
    }

    /**
     * Nested to mirror the shape of the {@code dashboardData} object built
     * inline in {@code getDashBoard} - kept as an inner class rather than a
     * top-level DTO since it has no meaning outside of this response.
     */
    public static class DashboardData {

        private long blogs;
        private long comments;
        private long drafts;
        private List<BlogResponse> recentBlogs;

        public DashboardData() {
        }

        public DashboardData(long blogs, long comments, long drafts, List<BlogResponse> recentBlogs) {
            this.blogs = blogs;
            this.comments = comments;
            this.drafts = drafts;
            this.recentBlogs = recentBlogs;
        }

        public long getBlogs() {
            return blogs;
        }

        public void setBlogs(long blogs) {
            this.blogs = blogs;
        }

        public long getComments() {
            return comments;
        }

        public void setComments(long comments) {
            this.comments = comments;
        }

        public long getDrafts() {
            return drafts;
        }

        public void setDrafts(long drafts) {
            this.drafts = drafts;
        }

        public List<BlogResponse> getRecentBlogs() {
            return recentBlogs;
        }

        public void setRecentBlogs(List<BlogResponse> recentBlogs) {
            this.recentBlogs = recentBlogs;
        }
    }
}
