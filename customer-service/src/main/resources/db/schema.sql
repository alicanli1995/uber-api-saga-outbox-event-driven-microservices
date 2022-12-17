create table if not exists public.locations
(
    id        uuid             not null,
    city      varchar(255),
    country   varchar(255),
    distance  double precision not null,
    latitude  double precision not null,
    longitude double precision not null,
    primary key (id)
);

alter table public.locations
    owner to postgres;

create table if not exists public.pending_requests
(
    request_id              uuid         not null,
    call_status             varchar(255),
    customer_email          varchar(255),
    driver_email            varchar(255),
    ip_address              varchar(255) not null,
    is_special_offer        boolean,
    offer                   double precision,
    payment_status          varchar(255),
    customer_destination_id uuid,
    customer_location_id    uuid,
    primary key (request_id),
    constraint fk9v5ulkylps8c9cyfnjy9b8ioc
        foreign key (customer_destination_id) references public.locations,
    constraint fkky6drob24cp4uhm0ha0dpjyys
        foreign key (customer_location_id) references public.locations
);

alter table public.pending_requests
    owner to postgres;


create table if not exists customer.balance_outbox
(
    id            uuid    not null,
    created_at    timestamp,
    outbox_status varchar(255),
    payload       varchar(2500),
    processed_at  timestamp,
    saga_id       uuid,
    saga_status   varchar(255),
    type          varchar(255),
    version       integer not null,
    primary key (id)
);

alter table customer.balance_outbox
    owner to postgres;

create table if not exists customer.customers
(
    id                         uuid not null,
    customer_status            varchar(255),
    email                      varchar(255),
    ip_address                 varchar(255),
    name                       varchar(255),
    locations_id               uuid,
    pending_request_request_id uuid,
    primary key (id),
    constraint fky62bo8l49h9e0ghfutme9b2d
        foreign key (locations_id) references public.locations,
    constraint fk5a34ibdx24ecqe9d2my8kg0o5
        foreign key (pending_request_request_id) references public.pending_requests
);

alter table customer.customers
    owner to postgres;

create table if not exists customer.driver_approval_outbox
(
    id            uuid    not null,
    created_at    timestamp,
    outbox_status varchar(255),
    payload       varchar(2500),
    processed_at  timestamp,
    saga_id       uuid,
    saga_status   varchar(255),
    type          varchar(255),
    version       integer not null,
    primary key (id)
);

alter table customer.driver_approval_outbox
    owner to postgres;

create table if not exists customer.user_extra
(
    username varchar(255) not null,
    avatar   varchar(255),
    primary key (username)
);

alter table customer.user_extra
    owner to postgres;

