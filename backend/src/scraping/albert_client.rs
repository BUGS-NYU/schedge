#![allow(dead_code)]

use crate::models::nyu::*;
use reqwest::Result;

/// Root URL of mobile Albert website
static ALBERT_ROOT: &'static str = "https://m.albert.nyu.edu/app/catalog/classSearch";
/// Root URL of mobile Albert website's data API
static ALBERT_DATA: &'static str = "https://m.albert.nyu.edu/app/catalog/getClassSearch";

/// Simple client for accessing NYU Albert.
///
/// Handles CSRF Token and accessing the correct website.
pub struct AlbertClient {
    csrf_token: String,
    client: reqwest::Client,
}

impl AlbertClient {
    pub fn new() -> Result<Self> {
        let client = reqwest::Client::new();
        let response = client.get(ALBERT_ROOT).send()?;
        let csrf_token = response
            .cookies()
            .find(|cookie| cookie.name() == "CSRFCookie")
            .expect(
                "Didn't find `CSRFCookie` after\
                 making request to ALBERT_ROOT",
            )
            .value()
            .into();
        Ok(Self { csrf_token, client })
    }

    fn post_request_xml(&self, term: TermID, school: &str, subject: &str) -> Result<String> {
        let term = term.as_u16();
        let referer_url = format!("{}/{}", ALBERT_ROOT, term);

        #[derive(Serialize)]
        struct FormBody<'a> {
            #[serde(rename = "CSRFToken")]
            csrf_token: &'a str,
            term: u16,
            acad_group: &'a str, // "UA" for CAS or "GU" for Global Public Health
            subject: &'a str,    // "CSCI-UA" for Computer Science at CAS
                                 // TODO rewrite subject and acad_group as enums
        }

        self.client
            .post(ALBERT_DATA)
            .form(&FormBody {
                csrf_token: &self.csrf_token,
                term,
                acad_group: school,
                subject,
            })
            .header(reqwest::header::REFERER, referer_url)
            .send()?
            .text()
    }
}
