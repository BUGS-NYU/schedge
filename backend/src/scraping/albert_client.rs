#![allow(dead_code)]

use reqwest::Result;

/// Root URL of mobile Albert website
static ALBERT_ROOT: &'static str = "https://m.albert.nyu.edu/app/catalog/classSearch";
/// Root URL of mobile Albert website's data API
static ALBERT_DATA: &'static str = "https://m.albert.nyu.edu/app/catalog/getClassSearch";

pub struct AlbertClient {
    csrf_token: String,
}

impl AlbertClient {
    pub fn new() -> Result<Self> {
        let response = reqwest::get(ALBERT_ROOT)?;
        let csrf_token = response
            .cookies()
            .find(|cookie| cookie.name() == "CSRFCookie")
            .expect(
                "Didn't find `CSRFCookie` after\
                 making request to ALBERT_ROOT",
            )
            .value()
            .into();
        Ok(Self { csrf_token })
    }
}
