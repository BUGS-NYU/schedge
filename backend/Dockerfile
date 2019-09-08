# From parent Rust dockerfile
FROM rustlang/rust:nightly as build
RUN apt-get update
RUN apt-get install clang -y

# Next 11 lines were copied from http://whitfin.io/speeding-up-rust-docker-builds/
# create a new empty shell project
RUN USER=root cargo new --bin schedge
WORKDIR /schedge

# copy over manifests
COPY ./Cargo.lock ./Cargo.lock
COPY ./Cargo.toml ./Cargo.toml

# copy over reset db script
COPY ./reset_db.sh ./reset_db.sh

# this build step will cache dependencies
RUN cargo build --release
RUN rm src/*.rs

# Copy over source files
# COPY ./tests ./tests
COPY ./src ./src

# Not sure about this one; apparently cargo doesn't rebuild, but
RUN rm ./target/release/deps/aletheia*
RUN cargo build --release

# our final base
FROM rustlang/rust:nightly

# copy the build artifact from the build stage
COPY --from=build /aletheia/target/release/aletheia .
COPY ./.env ./.env

# set the startup command to run your binary
CMD ["./aletheia"]
