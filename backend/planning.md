# Planning

## Use Cases
- Find courses you're interested in
- Plan out schedule for upcoming semester
- Blindly schedule intro courses
- Get a coherent schedule of courses you're interested in without having to figure out
  all the logistics
- Get a coherent schedule of courses you're interested in, that you're actually
  allowed to take
- Blindly schedule courses that you're elligible for

## General Backend Problems
- CORS
- Arbitrary course querying
  - GraphQL of db
- Generate schedule from arbitrary list of courses
- Generate custum schedule from arbitrary list of courses
- Traverse graph of courses, solving for prerequisites
- Generate recommended courses from already taken courses
- Generate schedule from arbitrary list of courses and courses already taken
- Topologically sort courses

## Backend Plan
- [ ] Use ruby script as model to build data pipeline for getting course info. Run as
  background process.
- [ ] Set up postgres as persistent storage mechanism of data.
- [ ] Integrate database into rocket
- [ ] Resolvers and algorithms as separate modules
- [ ] Build course request API
- [ ] Write simple algorithm that takes list of courses and returns singular schedule.
- [ ] ...

