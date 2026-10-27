package com.funewmanagementsystem.service;

import com.funewmanagementsystem.entity.ArticleStatus;
import com.funewmanagementsystem.entity.NewsArticle;
import com.funewmanagementsystem.entity.SystemAccount;


public interface ArticleWorkFlowService {

    NewsArticle transition(NewsArticle newsArticle, ArticleStatus articleStatus, SystemAccount systemAccount, String note);
}
